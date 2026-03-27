package net.ifmain.androiddummy.touch_pattern

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ElderlyTouchAnalyzerTest {

    private val analyzer = ElderlyTouchAnalyzer()

    @Test
    fun `analyzeElderlyProfile flags elderly profile for high risk characteristics`() {
        val profile = analyzer.analyzeElderlyProfile(
            TouchCharacteristics(
                avgPressure = 0.5f,
                pressureVariance = 0.35f,
                avgSpeed = 50f,
                maxSpeed = 90f,
                avgTouchSize = 0.4f,
                avgTapDuration = 260L,
                tremor = 0.8f,
                dragSmoothness = 0.2f,
                totalDistance = 120f,
                directDistance = 60f,
                pathEfficiency = 0.5f,
            )
        )

        assertTrue(profile.isLikelyElderly)
        assertTrue(profile.confidenceScore > 0.6f)
        assertTrue(profile.recommendations.contains("Enable high contrast mode for better visibility"))
        assertTrue(profile.recommendations.contains("Implement tremor filtering for more stable interactions"))
        assertEquals(profile.recommendations.distinct().size, profile.recommendations.size)
    }

    @Test
    fun `analyzeElderlyProfile stays low for fast and stable input`() {
        val profile = analyzer.analyzeElderlyProfile(
            TouchCharacteristics(
                avgPressure = 0.4f,
                pressureVariance = 0.05f,
                avgSpeed = 450f,
                maxSpeed = 600f,
                avgTouchSize = 0.1f,
                avgTapDuration = 60L,
                tremor = 0.05f,
                dragSmoothness = 0.9f,
                totalDistance = 120f,
                directDistance = 110f,
                pathEfficiency = 0.92f,
            )
        )

        assertFalse(profile.isLikelyElderly)
        assertTrue(profile.confidenceScore < 0.5f)
    }

    @Test
    fun `generateDetailedReport includes assessment and recommendations`() {
        val profile = analyzer.analyzeElderlyProfile(
            TouchCharacteristics(
                avgPressure = 0.5f,
                pressureVariance = 0.3f,
                avgSpeed = 50f,
                maxSpeed = 90f,
                avgTouchSize = 0.35f,
                avgTapDuration = 240L,
                tremor = 0.7f,
                dragSmoothness = 0.2f,
                totalDistance = 100f,
                directDistance = 60f,
                pathEfficiency = 0.6f,
            )
        )

        val report = analyzer.generateDetailedReport(profile)

        assertTrue(report.contains("Touch Pattern Analysis Report"))
        assertTrue(report.contains("Confidence Score"))
        assertTrue(report.contains("Recommendations:"))
    }
}
