package net.ifmain.androiddummy.touch_pattern

import android.app.Application
import android.view.MotionEvent
import androidx.test.core.app.ApplicationProvider
import net.ifmain.androiddummy.testutil.configureDisplayMetrics
import net.ifmain.androiddummy.testutil.singlePointerMotionEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HandednessAnalyzerTest {

    private val application = ApplicationProvider.getApplicationContext<Application>().also {
        configureDisplayMetrics(it, widthPixels = 1000, heightPixels = 2000)
    }

    @Test
    fun `insufficient touches remain undetermined`() {
        val analyzer = HandednessAnalyzer(application)

        repeat(10) { index ->
            analyzer.addTouchPoint(
                singlePointerMotionEvent(
                    action = MotionEvent.ACTION_DOWN,
                    x = 100f + index,
                    y = 1800f,
                    downTime = index.toLong(),
                    eventTime = index.toLong(),
                )
            )
        }

        val result = analyzer.analyzeHandedness()

        assertEquals(HandednessAnalyzer.Handedness.UNDETERMINED, result.handedness)
        assertEquals(0f, result.confidence, 0.0001f)
    }

    @Test
    fun `right side thumb zone bias resolves to right handed`() {
        val analyzer = HandednessAnalyzer(application)
        val baseTime = System.currentTimeMillis()

        repeat(60) { index ->
            analyzer.addTouchPoint(
                singlePointerMotionEvent(
                    action = MotionEvent.ACTION_DOWN,
                    x = 820f + (index % 5),
                    y = 1_780f,
                    downTime = baseTime + index,
                    eventTime = baseTime + index,
                    size = 0.35f,
                    touchMajor = 14f,
                    touchMinor = 12f,
                    edgeFlags = MotionEvent.EDGE_RIGHT,
                )
            )
        }

        repeat(12) {
            analyzer.addSwipePattern(
                startX = 650f,
                startY = 1_750f,
                endX = 900f,
                endY = 1_700f,
                velocity = 1_500f,
            )
        }

        val result = analyzer.analyzeHandedness()

        assertEquals(HandednessAnalyzer.Handedness.RIGHT_HANDED, result.handedness)
        assertTrue(result.analysis.rightSideScore > result.analysis.leftSideScore)
        assertTrue(result.analysis.thumbZoneDistribution["right"]!! > result.analysis.thumbZoneDistribution["left"]!!)
        assertTrue(result.confidence > 0.3f)
    }
}
