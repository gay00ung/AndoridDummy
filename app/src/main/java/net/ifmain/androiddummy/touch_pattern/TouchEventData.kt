package net.ifmain.androiddummy.touch_pattern

data class TouchEventData(
    val x: Float,
    val y: Float,
    val pressure: Float,
    val touchMajor: Float,
    val touchMinor: Float,
    val size: Float,
    val toolType: Int,
    val orientation: Float,
    val eventTime: Long,
    val downTime: Long,
    val action: Int,
    val pointerCount: Int,
    val edgeFlags: Int
)

data class TouchPattern(
    val downTime: Long,
    val eventTime: Long,
    val duration: Long,
    val tapInterval: Long? = null,
    val positions: List<Pair<Float, Float>>,
    val pressures: List<Float>,
    val velocities: List<Pair<Float, Float>>,
    val touchSizes: List<Float>
)

data class TouchCharacteristics(
    val avgPressure: Float,
    val pressureVariance: Float,
    val avgSpeed: Float,
    val maxSpeed: Float,
    val avgTouchSize: Float,
    val avgTapDuration: Long,
    val tremor: Float,
    val dragSmoothness: Float,
    val totalDistance: Float,
    val directDistance: Float,
    val pathEfficiency: Float
)

data class ElderlyTouchProfile(
    val isLikelyElderly: Boolean,
    val confidenceScore: Float,
    val characteristics: TouchCharacteristics,
    val recommendations: List<String>
)