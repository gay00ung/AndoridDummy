package net.ifmain.androiddummy.touch_pattern

import android.view.MotionEvent
import net.ifmain.androiddummy.testutil.singlePointerMotionEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GripPatternAnalyzerTest {

    @Test
    fun `high pressure small contact pattern resolves to pinch grip`() {
        val analyzer = GripPatternAnalyzer()
        val baseTime = System.currentTimeMillis()

        repeat(12) { index ->
            analyzer.analyzeTouch(
                singlePointerMotionEvent(
                    action = MotionEvent.ACTION_MOVE,
                    x = index * 3f,
                    y = 20f,
                    downTime = baseTime,
                    eventTime = baseTime + index * 16L,
                    pressure = 1f,
                    size = 0.05f,
                    touchMajor = 2f,
                    touchMinor = 2f,
                )
            )
        }

        val result = analyzer.analyzeGripPattern()

        assertEquals(GripPatternAnalyzer.GripType.PINCH_GRIP, result.gripType)
        assertTrue(result.analysis.averagePressure > 0.9f)
        assertTrue(result.analysis.contactAreaAverage < 50f)
        assertTrue(result.confidence > 0f)
    }

    @Test
    fun `reset clears histories and falls back to no grip`() {
        val analyzer = GripPatternAnalyzer()
        val baseTime = System.currentTimeMillis()

        repeat(12) { index ->
            analyzer.analyzeTouch(
                singlePointerMotionEvent(
                    action = MotionEvent.ACTION_MOVE,
                    x = index.toFloat(),
                    y = 0f,
                    downTime = baseTime,
                    eventTime = baseTime + index,
                )
            )
        }

        analyzer.reset()

        val result = analyzer.analyzeGripPattern()

        assertEquals(GripPatternAnalyzer.GripType.NO_GRIP, result.gripType)
        assertEquals(0f, result.analysis.averagePressure, 0.0001f)
        assertEquals(0.1f, result.confidence, 0.0001f)
    }
}
