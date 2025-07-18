package net.ifmain.androiddummy.touch_pattern

class ElderlyTouchAnalyzer {
    companion object {
        // Elderly touch pattern thresholds
        private const val ELDERLY_AVG_SPEED_THRESHOLD = 100f // pixels per second
        private const val ELDERLY_TAP_DURATION_THRESHOLD = 200L // milliseconds
        private const val ELDERLY_TOUCH_SIZE_THRESHOLD = 0.3f
        private const val ELDERLY_TREMOR_THRESHOLD = 0.5f
        private const val ELDERLY_PATH_EFFICIENCY_THRESHOLD = 0.7f
        private const val ELDERLY_PRESSURE_VARIANCE_THRESHOLD = 0.2f
    }
    
    fun analyzeElderlyProfile(characteristics: TouchCharacteristics): ElderlyTouchProfile {
        val scores = mutableListOf<Float>()
        val recommendations = mutableListOf<String>()
        
        // Speed analysis
        val speedScore = analyzeSpeed(characteristics.avgSpeed, characteristics.maxSpeed)
        scores.add(speedScore)
        if (speedScore > 0.7f) {
            recommendations.add("Consider increasing touch target sizes for easier interaction")
        }
        
        // Tap duration analysis
        val tapDurationScore = analyzeTapDuration(characteristics.avgTapDuration)
        scores.add(tapDurationScore)
        if (tapDurationScore > 0.7f) {
            recommendations.add("Extend tap recognition time to accommodate longer presses")
        }
        
        // Touch size analysis
        val touchSizeScore = analyzeTouchSize(characteristics.avgTouchSize)
        scores.add(touchSizeScore)
        if (touchSizeScore > 0.7f) {
            recommendations.add("Adjust touch sensitivity for larger contact areas")
        }
        
        // Tremor analysis
        val tremorScore = analyzeTremor(characteristics.tremor)
        scores.add(tremorScore)
        if (tremorScore > 0.7f) {
            recommendations.add("Implement tremor filtering for more stable interactions")
            recommendations.add("Consider gesture simplification")
        }
        
        // Path efficiency analysis
        val pathEfficiencyScore = analyzePathEfficiency(characteristics.pathEfficiency)
        scores.add(pathEfficiencyScore)
        if (pathEfficiencyScore > 0.7f) {
            recommendations.add("Enable gesture assistance for drag operations")
        }
        
        // Pressure variance analysis
        val pressureScore = analyzePressureVariance(characteristics.pressureVariance)
        scores.add(pressureScore)
        if (pressureScore > 0.7f) {
            recommendations.add("Normalize pressure sensitivity for consistent recognition")
        }
        
        // Calculate overall confidence score
        val confidenceScore = scores.average().toFloat()
        val isLikelyElderly = confidenceScore > 0.6f
        
        // Add general recommendations for elderly users
        if (isLikelyElderly) {
            recommendations.add("Enable high contrast mode for better visibility")
            recommendations.add("Increase font sizes system-wide")
            recommendations.add("Reduce animation speeds")
            recommendations.add("Enable haptic feedback for touch confirmation")
        }
        
        return ElderlyTouchProfile(
            isLikelyElderly = isLikelyElderly,
            confidenceScore = confidenceScore,
            characteristics = characteristics,
            recommendations = recommendations.distinct()
        )
    }
    
    private fun analyzeSpeed(avgSpeed: Float, maxSpeed: Float): Float {
        return when {
            avgSpeed < ELDERLY_AVG_SPEED_THRESHOLD -> 1.0f
            avgSpeed < ELDERLY_AVG_SPEED_THRESHOLD * 2 -> 0.7f
            avgSpeed < ELDERLY_AVG_SPEED_THRESHOLD * 3 -> 0.4f
            else -> 0.0f
        }
    }
    
    private fun analyzeTapDuration(avgTapDuration: Long): Float {
        return when {
            avgTapDuration > ELDERLY_TAP_DURATION_THRESHOLD -> 1.0f
            avgTapDuration > ELDERLY_TAP_DURATION_THRESHOLD * 0.75 -> 0.7f
            avgTapDuration > ELDERLY_TAP_DURATION_THRESHOLD * 0.5 -> 0.4f
            else -> 0.0f
        }
    }
    
    private fun analyzeTouchSize(avgTouchSize: Float): Float {
        return when {
            avgTouchSize > ELDERLY_TOUCH_SIZE_THRESHOLD -> 1.0f
            avgTouchSize > ELDERLY_TOUCH_SIZE_THRESHOLD * 0.8 -> 0.7f
            avgTouchSize > ELDERLY_TOUCH_SIZE_THRESHOLD * 0.6 -> 0.4f
            else -> 0.0f
        }
    }
    
    private fun analyzeTremor(tremor: Float): Float {
        return when {
            tremor > ELDERLY_TREMOR_THRESHOLD -> 1.0f
            tremor > ELDERLY_TREMOR_THRESHOLD * 0.7 -> 0.7f
            tremor > ELDERLY_TREMOR_THRESHOLD * 0.5 -> 0.4f
            else -> 0.0f
        }
    }
    
    private fun analyzePathEfficiency(pathEfficiency: Float): Float {
        return when {
            pathEfficiency < ELDERLY_PATH_EFFICIENCY_THRESHOLD -> 1.0f
            pathEfficiency < ELDERLY_PATH_EFFICIENCY_THRESHOLD * 1.2 -> 0.7f
            pathEfficiency < ELDERLY_PATH_EFFICIENCY_THRESHOLD * 1.4 -> 0.4f
            else -> 0.0f
        }
    }
    
    private fun analyzePressureVariance(pressureVariance: Float): Float {
        return when {
            pressureVariance > ELDERLY_PRESSURE_VARIANCE_THRESHOLD -> 1.0f
            pressureVariance > ELDERLY_PRESSURE_VARIANCE_THRESHOLD * 0.7 -> 0.7f
            pressureVariance > ELDERLY_PRESSURE_VARIANCE_THRESHOLD * 0.5 -> 0.4f
            else -> 0.0f
        }
    }
    
    fun generateDetailedReport(profile: ElderlyTouchProfile): String {
        return buildString {
            appendLine("=== Touch Pattern Analysis Report ===")
            appendLine()
            appendLine("Overall Assessment:")
            appendLine("- Elderly User Likelihood: ${if (profile.isLikelyElderly) "High" else "Low"}")
            appendLine("- Confidence Score: ${(profile.confidenceScore * 100).toInt()}%")
            appendLine()
            appendLine("Touch Characteristics:")
            appendLine("- Average Touch Speed: ${profile.characteristics.avgSpeed.toInt()} px/s")
            appendLine("- Average Tap Duration: ${profile.characteristics.avgTapDuration} ms")
            appendLine("- Average Touch Size: ${String.format("%.2f", profile.characteristics.avgTouchSize)}")
            appendLine("- Tremor Level: ${String.format("%.2f", profile.characteristics.tremor)}")
            appendLine("- Path Efficiency: ${(profile.characteristics.pathEfficiency * 100).toInt()}%")
            appendLine("- Pressure Variance: ${String.format("%.2f", profile.characteristics.pressureVariance)}")
            appendLine()
            appendLine("Recommendations:")
            profile.recommendations.forEachIndexed { index, recommendation ->
                appendLine("${index + 1}. $recommendation")
            }
        }
    }
}