package net.ifmain.androiddummy.touch_pattern

import android.content.Context
import android.view.MotionEvent
import kotlin.math.abs

/**
 * 오른손잡이/왼손잡이 판별 분석기
 */
class HandednessAnalyzer(private val context: Context) {
    private val touchPoints = mutableListOf<TouchData>()
    private val swipePatterns = mutableListOf<SwipeData>()
    
    data class TouchData(
        val x: Float,
        val y: Float,
        val pressure: Float,
        val touchMajor: Float,
        val touchMinor: Float,
        val edgeFlags: Int,
        val timestamp: Long
    )
    
    data class SwipeData(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val velocity: Float
    )
    
    data class HandednessResult(
        val handedness: Handedness,
        val confidence: Float,
        val analysis: HandednessAnalysis
    )
    
    data class HandednessAnalysis(
        val leftSideScore: Float,
        val rightSideScore: Float,
        val thumbZoneDistribution: Map<String, Int>,
        val edgeTouchRatio: Float,
        val swipeDirectionBias: Float
    )
    
    enum class Handedness {
        RIGHT_HANDED,
        LEFT_HANDED,
        AMBIDEXTROUS,
        UNDETERMINED
    }
    
    fun addTouchPoint(event: MotionEvent) {
        touchPoints.add(
            TouchData(
                x = event.x,
                y = event.y,
                pressure = event.pressure,
                touchMajor = event.touchMajor,
                touchMinor = event.touchMinor,
                edgeFlags = event.edgeFlags,
                timestamp = event.eventTime
            )
        )
        
        // 메모리 관리 - 최근 1000개만 유지
        if (touchPoints.size > 1000) {
            touchPoints.removeAt(0)
        }
    }
    
    fun addSwipePattern(startX: Float, startY: Float, endX: Float, endY: Float, velocity: Float) {
        swipePatterns.add(SwipeData(startX, startY, endX, endY, velocity))
        
        if (swipePatterns.size > 100) {
            swipePatterns.removeAt(0)
        }
    }
    
    fun analyzeHandedness(): HandednessResult {
        if (touchPoints.size < 50) {
            return HandednessResult(
                Handedness.UNDETERMINED,
                0f,
                HandednessAnalysis(0f, 0f, emptyMap(), 0f, 0f)
            )
        }
        
        val screenWidth = context.resources.displayMetrics.widthPixels
        val screenHeight = context.resources.displayMetrics.heightPixels
        
        // 1. 엄지손가락 위치 분석 (주로 하단 1/3 영역)
        val thumbZoneTouches = touchPoints.filter { 
            it.y > screenHeight * 0.66f 
        }
        
        // 2. 화면 좌우 터치 분포 분석
        val leftSideTouches = thumbZoneTouches.count { it.x < screenWidth * 0.5f }
        val rightSideTouches = thumbZoneTouches.count { it.x > screenWidth * 0.5f }
        
        // 3. 엣지 터치 패턴 분석
        val leftEdgeTouches = touchPoints.count { 
            it.edgeFlags and MotionEvent.EDGE_LEFT != 0 
        }
        val rightEdgeTouches = touchPoints.count { 
            it.edgeFlags and MotionEvent.EDGE_RIGHT != 0 
        }
        
        // 4. 스와이프 방향 분석
        val swipeDirectionBias = analyzeSwipeDirections()
        
        // 5. 터치 크기 분석 (엄지는 보통 더 큰 터치 영역)
        val leftThumbSizeTouches = thumbZoneTouches.filter { 
            it.x < screenWidth * 0.5f && it.touchMajor > 10f 
        }.size
        val rightThumbSizeTouches = thumbZoneTouches.filter { 
            it.x > screenWidth * 0.5f && it.touchMajor > 10f 
        }.size
        
        // 종합 점수 계산
        val leftScore = calculateScore(leftSideTouches, leftEdgeTouches, leftThumbSizeTouches, -swipeDirectionBias)
        val rightScore = calculateScore(rightSideTouches, rightEdgeTouches, rightThumbSizeTouches, swipeDirectionBias)
        
        val totalScore = leftScore + rightScore
        val leftRatio = if (totalScore > 0) leftScore / totalScore else 0.5f
        val rightRatio = if (totalScore > 0) rightScore / totalScore else 0.5f
        
        // 신뢰도 계산
        val confidence = calculateConfidence(leftRatio, rightRatio, touchPoints.size)
        
        // 최종 판정
        val handedness = when {
            abs(leftRatio - rightRatio) < 0.15f -> Handedness.AMBIDEXTROUS
            rightRatio > 0.65f -> Handedness.RIGHT_HANDED
            leftRatio > 0.65f -> Handedness.LEFT_HANDED
            else -> Handedness.UNDETERMINED
        }
        
        val analysis = HandednessAnalysis(
            leftSideScore = leftRatio,
            rightSideScore = rightRatio,
            thumbZoneDistribution = mapOf(
                "left" to leftSideTouches,
                "right" to rightSideTouches
            ),
            edgeTouchRatio = (leftEdgeTouches + rightEdgeTouches).toFloat() / touchPoints.size,
            swipeDirectionBias = swipeDirectionBias
        )
        
        return HandednessResult(handedness, confidence, analysis)
    }
    
    private fun analyzeSwipeDirections(): Float {
        if (swipePatterns.isEmpty()) return 0f
        
        var rightwardBias = 0f
        swipePatterns.forEach { swipe ->
            val deltaX = swipe.endX - swipe.startX
            rightwardBias += deltaX / swipePatterns.size
        }
        
        // 정규화 (-1 to 1)
        return rightwardBias.coerceIn(-1f, 1f)
    }
    
    private fun calculateScore(
        sideTouches: Int,
        edgeTouches: Int,
        thumbSizeTouches: Int,
        swipeBias: Float
    ): Float {
        return sideTouches * 1.0f +
               edgeTouches * 0.5f +
               thumbSizeTouches * 1.5f +
               swipeBias * 20f
    }
    
    private fun calculateConfidence(leftRatio: Float, rightRatio: Float, sampleSize: Int): Float {
        val ratioConfidence = abs(leftRatio - rightRatio) * 2f // 차이가 클수록 확신
        val sampleConfidence = (sampleSize.coerceAtMost(500) / 500f) // 샘플 수에 따른 신뢰도
        
        return (ratioConfidence * 0.7f + sampleConfidence * 0.3f).coerceIn(0f, 1f)
    }
    
    fun reset() {
        touchPoints.clear()
        swipePatterns.clear()
    }
}