package net.ifmain.androiddummy.touch_pattern

import android.view.MotionEvent
import kotlin.math.sqrt

/**
 * 폰 잡는 방식 분석기
 */
class GripPatternAnalyzer {
    private val edgeTouchHistory = mutableListOf<EdgeTouchEvent>()
    private val pressureHistory = mutableListOf<PressureEvent>()
    private val movementHistory = mutableListOf<MovementEvent>()
    
    data class EdgeTouchEvent(
        val edge: Int,
        val timestamp: Long,
        val pressure: Float,
        val touchSize: Float
    )
    
    data class PressureEvent(
        val pressure: Float,
        val touchMajor: Float,
        val touchMinor: Float,
        val timestamp: Long
    )
    
    data class MovementEvent(
        val x: Float,
        val y: Float,
        val timestamp: Long,
        val velocity: Float
    )
    
    data class GripAnalysisResult(
        val gripType: GripType,
        val confidence: Float,
        val analysis: GripAnalysis
    )
    
    data class GripAnalysis(
        val averagePressure: Float,
        val pressureVariance: Float,
        val edgeTouchFrequency: Map<String, Float>,
        val movementStability: Float,
        val contactAreaAverage: Float,
        val gripStrength: Float
    )
    
    enum class GripType {
        TIGHT_GRIP,        // 꽉 잡기
        LOOSE_GRIP,        // 느슨하게 잡기
        CRADLE_GRIP,       // 받쳐 잡기
        PINCH_GRIP,        // 집게 잡기
        PALM_GRIP,         // 손바닥 잡기
        FINGERTIP_GRIP,    // 손가락 끝 잡기
        NORMAL_GRIP,       // 일반 잡기
        NO_GRIP           // 평평한 곳에 놓음
    }
    
    fun analyzeTouch(event: MotionEvent) {
        // 엣지 터치 기록
        if (event.edgeFlags != 0) {
            recordEdgeTouch(event)
        }
        
        // 압력 기록
        recordPressure(event)
        
        // 움직임 기록
        recordMovement(event)
        
        // 메모리 관리
        cleanupOldData()
    }
    
    private fun recordEdgeTouch(event: MotionEvent) {
        edgeTouchHistory.add(
            EdgeTouchEvent(
                edge = event.edgeFlags,
                timestamp = event.eventTime,
                pressure = event.pressure,
                touchSize = event.size
            )
        )
    }
    
    private fun recordPressure(event: MotionEvent) {
        pressureHistory.add(
            PressureEvent(
                pressure = event.pressure,
                touchMajor = event.touchMajor,
                touchMinor = event.touchMinor,
                timestamp = event.eventTime
            )
        )
    }
    
    private fun recordMovement(event: MotionEvent) {
        if (movementHistory.isNotEmpty()) {
            val lastMove = movementHistory.last()
            val deltaTime = event.eventTime - lastMove.timestamp
            if (deltaTime > 0) {
                val distance = sqrt(
                    (event.x - lastMove.x) * (event.x - lastMove.x) +
                    (event.y - lastMove.y) * (event.y - lastMove.y)
                )
                val velocity = distance / deltaTime * 1000f // pixels per second
                
                movementHistory.add(
                    MovementEvent(
                        x = event.x,
                        y = event.y,
                        timestamp = event.eventTime,
                        velocity = velocity
                    )
                )
            }
        } else {
            movementHistory.add(
                MovementEvent(
                    x = event.x,
                    y = event.y,
                    timestamp = event.eventTime,
                    velocity = 0f
                )
            )
        }
    }
    
    private fun cleanupOldData() {
        val cutoffTime = System.currentTimeMillis() - 30000 // 30초 이전 데이터 제거
        
        edgeTouchHistory.removeAll { it.timestamp < cutoffTime }
        pressureHistory.removeAll { it.timestamp < cutoffTime }
        movementHistory.removeAll { it.timestamp < cutoffTime }
        
        // 최대 크기 제한
        if (edgeTouchHistory.size > 500) {
            edgeTouchHistory.subList(0, edgeTouchHistory.size - 500).clear()
        }
        if (pressureHistory.size > 1000) {
            pressureHistory.subList(0, pressureHistory.size - 1000).clear()
        }
        if (movementHistory.size > 1000) {
            movementHistory.subList(0, movementHistory.size - 1000).clear()
        }
    }
    
    fun analyzeGripPattern(): GripAnalysisResult {
        if (pressureHistory.size < 10) {
            return GripAnalysisResult(
                GripType.NO_GRIP,
                0.1f,
                GripAnalysis(0f, 0f, emptyMap(), 0f, 0f, 0f)
            )
        }
        
        // 1. 압력 분석
        val pressureAnalysis = analyzePressure()
        
        // 2. 엣지 터치 분석
        val edgeAnalysis = analyzeEdgeTouches()
        
        // 3. 움직임 안정성 분석
        val movementStability = calculateMovementStability()
        
        // 4. 접촉 면적 분석
        val contactAreaAnalysis = analyzeContactArea()
        
        // 5. 그립 강도 계산
        val gripStrength = calculateGripStrength(
            pressureAnalysis.average,
            edgeAnalysis.totalFrequency,
            contactAreaAnalysis
        )
        
        // 6. 그립 타입 판정
        val gripType = determineGripType(
            pressureAnalysis,
            edgeAnalysis,
            movementStability,
            contactAreaAnalysis,
            gripStrength
        )
        
        val analysis = GripAnalysis(
            averagePressure = pressureAnalysis.average,
            pressureVariance = pressureAnalysis.variance,
            edgeTouchFrequency = edgeAnalysis.frequencyMap,
            movementStability = movementStability,
            contactAreaAverage = contactAreaAnalysis,
            gripStrength = gripStrength
        )
        
        val confidence = calculateConfidence(analysis, gripType)
        
        return GripAnalysisResult(gripType, confidence, analysis)
    }
    
    private fun analyzePressure(): PressureAnalysis {
        val pressures = pressureHistory.map { it.pressure }
        val average = pressures.average().toFloat()
        val variance = calculateVariance(pressures, average)
        
        return PressureAnalysis(average, variance)
    }
    
    private fun calculateVariance(values: List<Float>, mean: Float): Float {
        if (values.isEmpty()) return 0f
        
        val squaredDiffs = values.map { (it - mean) * (it - mean) }
        return squaredDiffs.average().toFloat()
    }
    
    private fun analyzeEdgeTouches(): EdgeAnalysis {
        val edgeCount = mutableMapOf<String, Int>()
        
        edgeTouchHistory.forEach { event ->
            if (event.edge and MotionEvent.EDGE_TOP != 0) {
                edgeCount["top"] = edgeCount.getOrDefault("top", 0) + 1
            }
            if (event.edge and MotionEvent.EDGE_BOTTOM != 0) {
                edgeCount["bottom"] = edgeCount.getOrDefault("bottom", 0) + 1
            }
            if (event.edge and MotionEvent.EDGE_LEFT != 0) {
                edgeCount["left"] = edgeCount.getOrDefault("left", 0) + 1
            }
            if (event.edge and MotionEvent.EDGE_RIGHT != 0) {
                edgeCount["right"] = edgeCount.getOrDefault("right", 0) + 1
            }
        }
        
        val total = edgeCount.values.sum().toFloat()
        val frequencyMap = edgeCount.mapValues { (_, count) -> 
            if (total > 0) count / total else 0f 
        }
        
        return EdgeAnalysis(frequencyMap, total / (pressureHistory.size + 1))
    }
    
    private fun calculateMovementStability(): Float {
        if (movementHistory.size < 5) return 0.5f
        
        val velocities = movementHistory.takeLast(20).map { it.velocity }
        val avgVelocity = velocities.average()
        val velocityVariance = calculateVariance(velocities.map { it.toFloat() }, avgVelocity.toFloat())
        
        // 안정성은 속도 변화가 작을수록 높음
        return 1f / (1f + velocityVariance * 0.01f)
    }
    
    private fun analyzeContactArea(): Float {
        if (pressureHistory.isEmpty()) return 0f
        
        return pressureHistory
            .takeLast(50)
            .map { it.touchMajor * it.touchMinor }
            .average()
            .toFloat()
    }
    
    private fun calculateGripStrength(
        avgPressure: Float,
        edgeFrequency: Float,
        avgContactArea: Float
    ): Float {
        // 압력, 엣지 터치 빈도, 접촉 면적을 종합한 그립 강도
        val pressureScore = avgPressure
        val edgeScore = edgeFrequency.coerceIn(0f, 1f)
        val areaScore = (avgContactArea / 200f).coerceIn(0f, 1f) // 정규화
        
        return (pressureScore * 0.4f + edgeScore * 0.3f + areaScore * 0.3f)
    }
    
    private fun determineGripType(
        pressure: PressureAnalysis,
        edge: EdgeAnalysis,
        stability: Float,
        contactArea: Float,
        gripStrength: Float
    ): GripType {
        return when {
            // 꽉 잡기: 높은 압력, 많은 엣지 터치, 큰 접촉 면적
            gripStrength > 0.7f && 
            edge.totalFrequency > 0.3f -> GripType.TIGHT_GRIP
            
            // 느슨한 잡기: 낮은 압력, 불안정한 움직임
            pressure.average < 0.3f && 
            stability < 0.5f -> GripType.LOOSE_GRIP
            
            // 받침 잡기: 하단 엣지 터치 많음
            (edge.frequencyMap["bottom"] ?: 0f) > 0.4f -> GripType.CRADLE_GRIP
            
            // 집게 잡기: 작은 접촉 면적, 높은 압력
            contactArea < 50f && 
            pressure.average > 0.6f -> GripType.PINCH_GRIP
            
            // 손바닥 잡기: 큰 접촉 면적, 측면 엣지 터치
            contactArea > 150f && 
            ((edge.frequencyMap["left"] ?: 0f) + (edge.frequencyMap["right"] ?: 0f)) > 0.5f -> GripType.PALM_GRIP
            
            // 손가락 끝 잡기: 작은 접촉 면적, 적은 엣지 터치
            contactArea < 80f && 
            edge.totalFrequency < 0.1f -> GripType.FINGERTIP_GRIP
            
            // 평평한 곳에 놓음: 매우 안정적, 엣지 터치 없음
            stability > 0.9f && 
            edge.totalFrequency < 0.05f -> GripType.NO_GRIP
            
            else -> GripType.NORMAL_GRIP
        }
    }
    
    private fun calculateConfidence(analysis: GripAnalysis, gripType: GripType): Float {
        // 데이터 충분성에 기반한 신뢰도
        val dataSufficiency = minOf(
            pressureHistory.size / 100f,
            movementHistory.size / 100f,
            1f
        )
        
        // 패턴 명확성에 기반한 신뢰도
        val patternClarity = when (gripType) {
            GripType.TIGHT_GRIP -> if (analysis.gripStrength > 0.8f) 0.9f else 0.6f
            GripType.LOOSE_GRIP -> if (analysis.gripStrength < 0.3f) 0.9f else 0.6f
            GripType.NO_GRIP -> if (analysis.movementStability > 0.95f) 0.95f else 0.5f
            else -> 0.7f
        }
        
        return (dataSufficiency * 0.4f + patternClarity * 0.6f).coerceIn(0f, 1f)
    }
    
    fun reset() {
        edgeTouchHistory.clear()
        pressureHistory.clear()
        movementHistory.clear()
    }
    
    private data class PressureAnalysis(val average: Float, val variance: Float)
    private data class EdgeAnalysis(val frequencyMap: Map<String, Float>, val totalFrequency: Float)
}