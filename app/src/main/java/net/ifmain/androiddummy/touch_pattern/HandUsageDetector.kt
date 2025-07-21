package net.ifmain.androiddummy.touch_pattern

import android.content.Context
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 한손/두손 사용 패턴 감지기
 */
class HandUsageDetector(private val context: Context) {
    private val recentTouches = mutableListOf<TouchEvent>()
    private var averageTouchSize = 0f
    
    data class TouchEvent(
        val x: Float,
        val y: Float,
        val timestamp: Long,
        val touchSize: Float,
        val touchMajor: Float,
        val touchMinor: Float,
        val pointerCount: Int,
        val pointerId: Int
    )
    
    data class HandUsageResult(
        val usageType: HandUsageType,
        val confidence: Float,
        val analysis: HandUsageAnalysis
    )
    
    data class HandUsageAnalysis(
        val touchSpreadWidth: Float,
        val touchSpreadHeight: Float,
        val averageReachDistance: Float,
        val multiTouchFrequency: Float,
        val thumbPatternScore: Float,
        val indexPatternScore: Float
    )
    
    enum class HandUsageType {
        ONE_HAND_THUMB,      // 한손 엄지
        ONE_HAND_INDEX,      // 한손 검지  
        ONE_HAND_MULTI_FINGER, // 한손 여러 손가락
        TWO_HANDS,           // 양손
        STYLUS,              // 스타일러스
        UNDETERMINED         // 미확정
    }
    
    fun detectHandUsage(event: MotionEvent): HandUsageResult {
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
        
        // 터치 이벤트 기록
        recordTouchEvent(event)
        
        // 평균 터치 크기 업데이트
        updateAverageTouchSize()
        
        // 스타일러스 체크
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS) {
            return HandUsageResult(
                HandUsageType.STYLUS,
                1.0f,
                HandUsageAnalysis(0f, 0f, 0f, 0f, 0f, 0f)
            )
        }
        
        // 1. 다중 터치 확인
        if (event.pointerCount > 1) {
            val distance = calculatePointerDistance(event)
            val usageType = if (distance > screenWidth * 0.4f) {
                HandUsageType.TWO_HANDS
            } else {
                HandUsageType.ONE_HAND_MULTI_FINGER
            }
            
            val analysis = analyzeMultiTouch(event, screenWidth, screenHeight)
            return HandUsageResult(usageType, 0.9f, analysis)
        }
        
        // 2. 터치 도달 범위 분석
        val touchSpread = calculateTouchSpread()
        val reachableZone = calculateReachableZone(screenWidth, screenHeight)
        
        // 3. 최근 터치 패턴 분석
        val analysis = HandUsageAnalysis(
            touchSpreadWidth = touchSpread.width,
            touchSpreadHeight = touchSpread.height,
            averageReachDistance = calculateAverageReachDistance(),
            multiTouchFrequency = calculateMultiTouchFrequency(),
            thumbPatternScore = calculateThumbPattern(),
            indexPatternScore = calculateIndexPattern()
        )
        
        // 4. 사용 패턴 판정
        val usageType = when {
            touchSpread.width > screenWidth * 0.7f -> HandUsageType.TWO_HANDS
            touchSpread.height > screenHeight * 0.6f -> HandUsageType.TWO_HANDS
            isThumbPattern() -> HandUsageType.ONE_HAND_THUMB
            isIndexPattern() -> HandUsageType.ONE_HAND_INDEX
            recentTouches.size < 10 -> HandUsageType.UNDETERMINED
            else -> HandUsageType.ONE_HAND_THUMB
        }
        
        val confidence = calculateConfidence(analysis, usageType)
        
        return HandUsageResult(usageType, confidence, analysis)
    }
    
    private fun recordTouchEvent(event: MotionEvent) {
        for (i in 0 until event.pointerCount) {
            recentTouches.add(
                TouchEvent(
                    x = event.getX(i),
                    y = event.getY(i),
                    timestamp = event.eventTime,
                    touchSize = event.getSize(i),
                    touchMajor = event.getTouchMajor(i),
                    touchMinor = event.getTouchMinor(i),
                    pointerCount = event.pointerCount,
                    pointerId = event.getPointerId(i)
                )
            )
        }
        
        // 최근 500개만 유지
        while (recentTouches.size > 500) {
            recentTouches.removeAt(0)
        }
    }
    
    private fun updateAverageTouchSize() {
        if (recentTouches.isEmpty()) return
        
        averageTouchSize = recentTouches
            .takeLast(50)
            .map { it.touchSize }
            .average()
            .toFloat()
    }
    
    private fun calculatePointerDistance(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f
        
        val x1 = event.getX(0)
        val y1 = event.getY(0)
        val x2 = event.getX(1)
        val y2 = event.getY(1)
        
        return sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    }
    
    private fun calculateTouchSpread(): TouchSpread {
        if (recentTouches.isEmpty()) return TouchSpread(0f, 0f)
        
        val recent = recentTouches.takeLast(100)
        val minX = recent.minOf { it.x }
        val maxX = recent.maxOf { it.x }
        val minY = recent.minOf { it.y }
        val maxY = recent.maxOf { it.y }
        
        return TouchSpread(maxX - minX, maxY - minY)
    }
    
    private fun calculateReachableZone(screenWidth: Float, screenHeight: Float): ReachableZone {
        // 일반적인 엄지손가락 도달 범위 계산
        val thumbReachRadius = screenWidth * 0.4f
        val centerX = screenWidth * 0.5f
        val centerY = screenHeight * 0.8f
        
        return ReachableZone(centerX, centerY, thumbReachRadius)
    }
    
    private fun calculateAverageReachDistance(): Float {
        if (recentTouches.isEmpty()) return 0f
        
        val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
        val bottomY = screenHeight * 0.9f
        
        return recentTouches
            .takeLast(50)
            .map { abs(bottomY - it.y) }
            .average()
            .toFloat()
    }
    
    private fun calculateMultiTouchFrequency(): Float {
        if (recentTouches.isEmpty()) return 0f
        
        val multiTouchCount = recentTouches.count { it.pointerCount > 1 }
        return multiTouchCount.toFloat() / recentTouches.size
    }
    
    private fun isThumbPattern(): Boolean {
        // 엄지 특성: 큰 터치 영역, 곡선 움직임, 화면 하단 중심
        val recent = recentTouches.takeLast(20)
        if (recent.isEmpty()) return false
        
        val avgSize = recent.map { it.touchMajor }.average()
        val screenHeight = context.resources.displayMetrics.heightPixels
        val lowerTouches = recent.count { it.y > screenHeight * 0.6f }
        
        return avgSize > averageTouchSize * 1.2f && 
               lowerTouches > recent.size * 0.7f
    }
    
    private fun isIndexPattern(): Boolean {
        // 검지 특성: 작은 터치 영역, 직선적 움직임, 더 넓은 범위
        val recent = recentTouches.takeLast(20)
        if (recent.isEmpty()) return false
        
        val avgSize = recent.map { it.touchMajor }.average()
        val movementLinearity = calculateMovementLinearity(recent)
        
        return avgSize < averageTouchSize * 0.9f && 
               movementLinearity > 0.7f
    }
    
    private fun calculateThumbPattern(): Float {
        if (!isThumbPattern()) return 0f
        
        val recent = recentTouches.takeLast(30)
        val sizeScore = (recent.map { it.touchMajor }.average() / (averageTouchSize + 0.1f)).toFloat()
        val positionScore = calculateLowerScreenUsage(recent)
        
        return (sizeScore * 0.6f + positionScore * 0.4f).coerceIn(0f, 1f)
    }
    
    private fun calculateIndexPattern(): Float {
        if (!isIndexPattern()) return 0f
        
        val recent = recentTouches.takeLast(30)
        val sizeScore = (1f - (recent.map { it.touchMajor }.average() / (averageTouchSize + 0.1f)).toFloat())
        val linearityScore = calculateMovementLinearity(recent)
        
        return (sizeScore * 0.4f + linearityScore * 0.6f).coerceIn(0f, 1f)
    }
    
    private fun calculateMovementLinearity(touches: List<TouchEvent>): Float {
        if (touches.size < 3) return 0f
        
        var totalDeviation = 0f
        for (i in 1 until touches.size - 1) {
            val prev = touches[i - 1]
            val curr = touches[i]
            val next = touches[i + 1]
            
            // 직선에서의 편차 계산
            val expectedX = prev.x + (next.x - prev.x) * 0.5f
            val expectedY = prev.y + (next.y - prev.y) * 0.5f
            
            val deviation = sqrt((curr.x - expectedX) * (curr.x - expectedX) + 
                               (curr.y - expectedY) * (curr.y - expectedY))
            totalDeviation += deviation
        }
        
        val avgDeviation = totalDeviation / (touches.size - 2)
        return 1f / (1f + avgDeviation * 0.01f) // 편차가 작을수록 1에 가까움
    }
    
    private fun calculateLowerScreenUsage(touches: List<TouchEvent>): Float {
        if (touches.isEmpty()) return 0f
        
        val screenHeight = context.resources.displayMetrics.heightPixels
        val lowerCount = touches.count { it.y > screenHeight * 0.6f }
        
        return lowerCount.toFloat() / touches.size
    }
    
    private fun analyzeMultiTouch(event: MotionEvent, screenWidth: Float, screenHeight: Float): HandUsageAnalysis {
        val touchSpread = calculateTouchSpread()
        
        return HandUsageAnalysis(
            touchSpreadWidth = touchSpread.width,
            touchSpreadHeight = touchSpread.height,
            averageReachDistance = calculateAverageReachDistance(),
            multiTouchFrequency = 1.0f, // 현재 멀티터치 중
            thumbPatternScore = 0f,
            indexPatternScore = 0f
        )
    }
    
    private fun calculateConfidence(analysis: HandUsageAnalysis, usageType: HandUsageType): Float {
        return when (usageType) {
            HandUsageType.TWO_HANDS -> {
                val spreadConfidence = (analysis.touchSpreadWidth / context.resources.displayMetrics.widthPixels).coerceIn(0f, 1f)
                val multiTouchConfidence = analysis.multiTouchFrequency
                (spreadConfidence * 0.6f + multiTouchConfidence * 0.4f).coerceIn(0f, 1f)
            }
            HandUsageType.ONE_HAND_THUMB -> {
                analysis.thumbPatternScore
            }
            HandUsageType.ONE_HAND_INDEX -> {
                analysis.indexPatternScore
            }
            HandUsageType.ONE_HAND_MULTI_FINGER -> {
                0.8f // 멀티터치가 감지되면 높은 확신도
            }
            HandUsageType.STYLUS -> {
                1.0f // 스타일러스는 확실함
            }
            HandUsageType.UNDETERMINED -> {
                0.2f
            }
        }
    }
    
    fun reset() {
        recentTouches.clear()
        averageTouchSize = 0f
    }
    
    private data class TouchSpread(val width: Float, val height: Float)
    private data class ReachableZone(val centerX: Float, val centerY: Float, val radius: Float)
}