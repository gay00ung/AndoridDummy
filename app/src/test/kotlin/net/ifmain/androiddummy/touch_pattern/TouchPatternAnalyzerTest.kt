package net.ifmain.androiddummy.touch_pattern

import android.view.MotionEvent
import net.ifmain.androiddummy.testutil.HistoricalTouchPoint
import net.ifmain.androiddummy.testutil.singlePointerMotionEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TouchPatternAnalyzerTest {

    @Test
    fun `analyzeCharacteristics returns zero values without patterns`() {
        val analyzer = TouchPatternAnalyzer()

        val characteristics = analyzer.analyzeCharacteristics()

        assertEquals(0f, characteristics.avgPressure, 0.0001f)
        assertEquals(0f, characteristics.avgSpeed, 0.0001f)
        assertEquals(0f, characteristics.totalDistance, 0.0001f)
        assertEquals(0L, characteristics.avgTapDuration)
    }

    @Test
    fun `tracking drag produces nonzero speed and distance metrics`() {
        val analyzer = TouchPatternAnalyzer()
        val downTime = 1_000L

        analyzer.startTracking()
        analyzer.processEvent(
            singlePointerMotionEvent(
                action = MotionEvent.ACTION_DOWN,
                x = 0f,
                y = 0f,
                downTime = downTime,
                eventTime = downTime,
                pressure = 0.4f,
                size = 0.2f,
            )
        )
        analyzer.processEvent(
            singlePointerMotionEvent(
                action = MotionEvent.ACTION_MOVE,
                x = 20f,
                y = 0f,
                downTime = downTime,
                eventTime = downTime + 10L,
                pressure = 0.5f,
                size = 0.25f,
            )
        )
        analyzer.processEvent(
            singlePointerMotionEvent(
                action = MotionEvent.ACTION_UP,
                x = 40f,
                y = 0f,
                downTime = downTime,
                eventTime = downTime + 20L,
                pressure = 0.6f,
                size = 0.3f,
                history = listOf(
                    HistoricalTouchPoint(
                        eventTime = downTime + 5L,
                        x = 10f,
                        y = 0f,
                        pressure = 0.45f,
                        size = 0.22f,
                    ),
                    HistoricalTouchPoint(
                        eventTime = downTime + 15L,
                        x = 30f,
                        y = 0f,
                        pressure = 0.55f,
                        size = 0.28f,
                    ),
                ),
            )
        )

        val patterns = analyzer.getTouchPatterns()
        val characteristics = analyzer.analyzeCharacteristics()

        assertEquals(1, patterns.size)
        assertTrue(patterns.single().duration > 0L)
        assertEquals(3, patterns.single().positions.size)
        assertTrue(characteristics.avgPressure > 0f)
        assertTrue(characteristics.avgSpeed > 0f)
        assertTrue(characteristics.maxSpeed >= characteristics.avgSpeed)
        assertTrue(characteristics.totalDistance > 0f)
        assertTrue(characteristics.directDistance > 0f)
        assertTrue(characteristics.pathEfficiency in 0f..1f)

        analyzer.stopTracking()
    }
}
