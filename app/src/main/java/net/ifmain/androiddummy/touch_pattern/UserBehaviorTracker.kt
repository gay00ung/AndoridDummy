package net.ifmain.androiddummy.touch_pattern

import android.content.Context
import android.view.MotionEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

/**
 * 사용자 행동 패턴 실시간 추적 및 분석
 */
class UserBehaviorTracker(context: Context) {
    private val sessionData = SessionData()
    private val handednessAnalyzer = HandednessAnalyzer(context)
    private val handUsageDetector = HandUsageDetector(context)
    private val gripPatternAnalyzer = GripPatternAnalyzer()
    private val elderlyAnalyzer = ElderlyTouchAnalyzer()
    
    private val _behaviorAnalysis = MutableStateFlow<UserBehaviorAnalysis?>(null)
    val behaviorAnalysis: StateFlow<UserBehaviorAnalysis?> = _behaviorAnalysis
    
    data class SessionData(
        val touches: MutableList<TouchProfile> = mutableListOf(),
        val startTime: Long = System.currentTimeMillis(),
        var lastAnalysisTime: Long = 0L
    )
    
    data class TouchProfile(
        val x: Float,
        val y: Float,
        val pressure: Float,
        val size: Float,
        val touchMajor: Float,
        val touchMinor: Float,
        val toolType: Int,
        val edgeFlags: Int,
        val timestamp: Long,
        val action: Int,
        val pointerCount: Int,
        val velocityX: Float = 0f,
        val velocityY: Float = 0f
    )
    
    data class UserBehaviorAnalysis(
        val handedness: HandednessAnalyzer.HandednessResult,
        val handUsage: HandUsageDetector.HandUsageResult,
        val gripType: GripPatternAnalyzer.GripAnalysisResult,
        val elderlyProfile: ElderlyTouchProfile?,
        val stressLevel: StressLevel,
        val behaviorConfidence: Float,
        val sessionDuration: Long,
        val touchCount: Int,
        val anomalyScore: Float,
        val recommendations: List<String>
    )
    
    data class StressLevel(
        val level: Float, // 0.0 - 1.0
        val category: StressCategory,
        val indicators: StressIndicators
    )
    
    data class StressIndicators(
        val avgPressure: Float,
        val tapFrequency: Float,
        val movementIrregularity: Float,
        val touchDurationVariance: Float
    )
    
    enum class StressCategory {
        RELAXED,      // 편안함
        NORMAL,       // 보통
        FOCUSED,      // 집중
        STRESSED,     // 스트레스
        FRUSTRATED    // 좌절/화남
    }
    
    fun processTouch(event: MotionEvent, velocityX: Float = 0f, velocityY: Float = 0f) {
        // 터치 프로필 생성 및 저장
        val touchProfile = TouchProfile(
            x = event.x,
            y = event.y,
            pressure = event.pressure,
            size = event.size,
            touchMajor = event.touchMajor,
            touchMinor = event.touchMinor,
            toolType = event.getToolType(0),
            edgeFlags = event.edgeFlags,
            timestamp = event.eventTime,
            action = event.action,
            pointerCount = event.pointerCount,
            velocityX = velocityX,
            velocityY = velocityY
        )
        
        sessionData.touches.add(touchProfile)
        
        // 각 분석기에 데이터 전달
        handednessAnalyzer.addTouchPoint(event)
        handUsageDetector.detectHandUsage(event)
        gripPatternAnalyzer.analyzeTouch(event)
        
        // 터치 패턴 분석기에도 전달
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (velocityX != 0f || velocityY != 0f) {
                    handednessAnalyzer.addSwipePattern(
                        event.x, event.y, event.x + velocityX * 0.1f, event.y + velocityY * 0.1f, 
                        kotlin.math.sqrt(velocityX * velocityX + velocityY * velocityY)
                    )
                }
            }
        }
        
        // 실시간 분석 (20개 이상 터치 시 시작, 1초마다 업데이트)
        if (sessionData.touches.size >= 20 && 
            System.currentTimeMillis() - sessionData.lastAnalysisTime > 1000) {
            analyzeUserBehavior()
        }
        
        // 메모리 관리
        if (sessionData.touches.size > 2000) {
            sessionData.touches.subList(0, 1000).clear()
        }
    }
    
    private fun analyzeUserBehavior() {
        sessionData.lastAnalysisTime = System.currentTimeMillis()
        
        // 1. 손잡이 분석
        val handednessResult = handednessAnalyzer.analyzeHandedness()
        
        // 2. 손 사용 패턴 분석
        val handUsageResult = handUsageDetector.detectHandUsage(
            createMotionEventFromLastTouch()
        )
        
        // 3. 그립 패턴 분석
        val gripResult = gripPatternAnalyzer.analyzeGripPattern()
        
        // 4. 노인 패턴 분석
        val elderlyProfile = analyzeElderlyPattern()
        
        // 5. 스트레스 레벨 분석
        val stressLevel = calculateStressLevel()
        
        // 6. 이상 행동 점수 계산
        val anomalyScore = calculateAnomalyScore()
        
        // 7. 권장사항 생성
        val recommendations = generateRecommendations(
            handednessResult,
            handUsageResult,
            gripResult,
            elderlyProfile,
            stressLevel
        )
        
        // 8. 전체 신뢰도 계산
        val behaviorConfidence = calculateOverallConfidence(
            handednessResult.confidence,
            handUsageResult.confidence,
            gripResult.confidence,
            elderlyProfile?.confidenceScore ?: 0.5f
        )
        
        val analysis = UserBehaviorAnalysis(
            handedness = handednessResult,
            handUsage = handUsageResult,
            gripType = gripResult,
            elderlyProfile = elderlyProfile,
            stressLevel = stressLevel,
            behaviorConfidence = behaviorConfidence,
            sessionDuration = System.currentTimeMillis() - sessionData.startTime,
            touchCount = sessionData.touches.size,
            anomalyScore = anomalyScore,
            recommendations = recommendations
        )
        
        _behaviorAnalysis.value = analysis
    }
    
    private fun createMotionEventFromLastTouch(): MotionEvent {
        val lastTouch = sessionData.touches.lastOrNull() ?: return MotionEvent.obtain(
            0, 0, MotionEvent.ACTION_DOWN, 0f, 0f, 0
        )
        
        // MotionEvent.obtain with pressure parameter
        return MotionEvent.obtain(
            sessionData.startTime,
            lastTouch.timestamp,
            lastTouch.action,
            lastTouch.x,
            lastTouch.y,
            lastTouch.pressure,
            lastTouch.size,
            0,  // metaState
            0f, // xPrecision
            0f, // yPrecision
            0,  // deviceId
            0   // edgeFlags
        )
    }
    
    private fun analyzeElderlyPattern(): ElderlyTouchProfile? {
        val recentTouches = sessionData.touches.takeLast(100)
        if (recentTouches.size < 20) return null
        
        // TouchPatternAnalyzer를 사용해 패턴 분석
        val analyzer = TouchPatternAnalyzer()
        analyzer.startTracking()
        
        // 터치 이벤트를 MotionEvent로 변환하여 분석
        recentTouches.forEach { touch ->
            val event = MotionEvent.obtain(
                sessionData.startTime,
                touch.timestamp,
                touch.action,
                touch.x,
                touch.y,
                touch.pressure,
                touch.size,
                0,  // metaState
                0f, // xPrecision
                0f, // yPrecision
                0,  // deviceId
                touch.edgeFlags
            )
            analyzer.processEvent(event)
            event.recycle()
        }
        
        // 분석된 특성 가져오기
        val characteristics = analyzer.analyzeCharacteristics()
        analyzer.stopTracking()
        
        // ElderlyTouchAnalyzer를 사용해 노인 패턴 분석
        return elderlyAnalyzer.analyzeElderlyProfile(characteristics)
    }
    
    private fun calculateStressLevel(): StressLevel {
        val recentTouches = sessionData.touches.takeLast(100)
        if (recentTouches.size < 10) {
            return StressLevel(
                0.3f,
                StressCategory.NORMAL,
                StressIndicators(0.5f, 0.3f, 0.3f, 0.3f)
            )
        }
        
        // 압력 분석
        val avgPressure = recentTouches.map { it.pressure }.average().toFloat()
        
        // 탭 빈도 분석
        val tapActions = recentTouches.count { it.action == MotionEvent.ACTION_DOWN }
        val timeSpan = (recentTouches.last().timestamp - recentTouches.first().timestamp) / 1000f
        val tapFrequency = if (timeSpan > 0) tapActions / timeSpan else 0f
        
        // 움직임 불규칙성 분석
        val movementIrregularity = calculateMovementIrregularity(recentTouches)
        
        // 터치 지속 시간 분산
        val touchDurations = calculateTouchDurations(recentTouches)
        val durationVariance = calculateVariance(touchDurations)
        
        val indicators = StressIndicators(
            avgPressure = avgPressure,
            tapFrequency = tapFrequency.coerceIn(0f, 10f) / 10f, // 정규화
            movementIrregularity = movementIrregularity,
            touchDurationVariance = durationVariance
        )
        
        // 종합 스트레스 점수
        val stressScore = (
            avgPressure * 0.3f +
            indicators.tapFrequency * 0.3f +
            movementIrregularity * 0.2f +
            durationVariance * 0.2f
        ).coerceIn(0f, 1f)
        
        val category = when {
            stressScore < 0.2f -> StressCategory.RELAXED
            stressScore < 0.4f -> StressCategory.NORMAL
            stressScore < 0.6f -> StressCategory.FOCUSED
            stressScore < 0.8f -> StressCategory.STRESSED
            else -> StressCategory.FRUSTRATED
        }
        
        return StressLevel(stressScore, category, indicators)
    }
    
    private fun calculateMovementIrregularity(touches: List<TouchProfile>): Float {
        if (touches.size < 3) return 0f
        
        val movements = touches.windowed(2).map { (prev, curr) ->
            kotlin.math.sqrt(
                (curr.x - prev.x) * (curr.x - prev.x) +
                (curr.y - prev.y) * (curr.y - prev.y)
            )
        }
        
        val avgMovement = movements.average()
        val variance = movements.map { (it - avgMovement) * (it - avgMovement) }.average()
        
        return (variance / (avgMovement + 1f)).toFloat().coerceIn(0f, 1f)
    }
    
    private fun calculateTouchDurations(touches: List<TouchProfile>): List<Float> {
        val durations = mutableListOf<Float>()
        var downTime = 0L
        
        touches.forEach { touch ->
            when (touch.action) {
                MotionEvent.ACTION_DOWN -> downTime = touch.timestamp
                MotionEvent.ACTION_UP -> {
                    if (downTime > 0) {
                        durations.add((touch.timestamp - downTime).toFloat())
                        downTime = 0
                    }
                }
            }
        }
        
        return durations
    }
    
    private fun calculateVariance(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        val mean = values.average()
        return values.map { (it - mean) * (it - mean) }.average().toFloat() / 1000f // 정규화
    }
    
    private fun calculateAnomalyScore(): Float {
        // 정상 패턴과의 차이를 측정
        val recentTouches = sessionData.touches.takeLast(50)
        if (recentTouches.size < 10) return 0f
        
        var anomalyScore = 0f
        
        // 1. 비정상적인 압력 패턴
        val pressures = recentTouches.map { it.pressure }
        val allSamePressure = pressures.distinct().size == 1
        if (allSamePressure && pressures.first() == 1.0f) {
            anomalyScore += 0.3f // 항상 최대 압력은 의심스러움
        }
        
        // 2. 비현실적인 속도
        val highSpeedCount = recentTouches.count { 
            kotlin.math.sqrt(it.velocityX * it.velocityX + it.velocityY * it.velocityY) > 5000 
        }
        anomalyScore += (highSpeedCount.toFloat() / recentTouches.size) * 0.3f
        
        // 3. 완벽한 직선/패턴
        val perfectPattern = checkPerfectPattern(recentTouches)
        if (perfectPattern) {
            anomalyScore += 0.4f
        }
        
        return anomalyScore.coerceIn(0f, 1f)
    }
    
    private fun checkPerfectPattern(touches: List<TouchProfile>): Boolean {
        if (touches.size < 5) return false
        
        // 완벽한 직선이나 원 등의 패턴 검사
        val positions = touches.map { it.x to it.y }
        val distances = positions.windowed(2).map { (prev, curr) ->
            kotlin.math.sqrt(
                (curr.first - prev.first) * (curr.first - prev.first) +
                (curr.second - prev.second) * (curr.second - prev.second)
            )
        }
        
        // 모든 거리가 거의 동일한지 확인
        val avgDistance = distances.average()
        val maxDeviation = distances.maxOfOrNull { abs(it - avgDistance) } ?: 0.0
        
        return maxDeviation < avgDistance * 0.05 // 5% 이내 편차
    }
    
    private fun generateRecommendations(
        handedness: HandednessAnalyzer.HandednessResult,
        handUsage: HandUsageDetector.HandUsageResult,
        grip: GripPatternAnalyzer.GripAnalysisResult,
        elderly: ElderlyTouchProfile?,
        stress: StressLevel
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // 손잡이 기반 권장사항
        when (handedness.handedness) {
            HandednessAnalyzer.Handedness.LEFT_HANDED -> {
                recommendations.add("왼손잡이용 UI 레이아웃 제공")
                recommendations.add("중요 버튼을 화면 왼쪽에 배치")
            }
            HandednessAnalyzer.Handedness.RIGHT_HANDED -> {
                recommendations.add("오른손잡이용 표준 레이아웃 유지")
            }
            HandednessAnalyzer.Handedness.AMBIDEXTROUS -> {
                recommendations.add("양손 사용자를 위한 대칭형 UI 제공")
            }
            else -> {}
        }
        
        // 손 사용 패턴 기반
        when (handUsage.usageType) {
            HandUsageDetector.HandUsageType.ONE_HAND_THUMB -> {
                recommendations.add("하단 영역에 주요 기능 배치")
                recommendations.add("스와이프 제스처 활용")
            }
            HandUsageDetector.HandUsageType.TWO_HANDS -> {
                recommendations.add("화면 전체 활용 가능")
                recommendations.add("멀티터치 제스처 활성화")
            }
            else -> {}
        }
        
        // 그립 패턴 기반
        when (grip.gripType) {
            GripPatternAnalyzer.GripType.LOOSE_GRIP -> {
                recommendations.add("실수 터치 방지 기능 활성화")
                recommendations.add("터치 영역 확대")
            }
            GripPatternAnalyzer.GripType.TIGHT_GRIP -> {
                recommendations.add("압력 감도 조정 필요")
            }
            else -> {}
        }
        
        // 노인 사용자
        elderly?.recommendations?.let {
            recommendations.addAll(it)
        }
        
        // 스트레스 레벨
        when (stress.category) {
            StressCategory.STRESSED, StressCategory.FRUSTRATED -> {
                recommendations.add("단순화된 UI 모드 제공")
                recommendations.add("오류 메시지 개선")
                recommendations.add("도움말 접근성 향상")
            }
            else -> {}
        }
        
        return recommendations.distinct()
    }
    
    private fun calculateOverallConfidence(vararg confidences: Float): Float {
        return confidences.average().toFloat()
    }
    
    fun reset() {
        sessionData.touches.clear()
        sessionData.lastAnalysisTime = 0L
        handednessAnalyzer.reset()
        handUsageDetector.reset()
        gripPatternAnalyzer.reset()
        _behaviorAnalysis.value = null
    }
    
    fun getSessionSummary(): SessionSummary {
        return SessionSummary(
            duration = System.currentTimeMillis() - sessionData.startTime,
            touchCount = sessionData.touches.size,
            lastAnalysis = _behaviorAnalysis.value,
            anomalyDetected = (_behaviorAnalysis.value?.anomalyScore ?: 0f) > 0.7f
        )
    }
    
    data class SessionSummary(
        val duration: Long,
        val touchCount: Int,
        val lastAnalysis: UserBehaviorAnalysis?,
        val anomalyDetected: Boolean
    )
}