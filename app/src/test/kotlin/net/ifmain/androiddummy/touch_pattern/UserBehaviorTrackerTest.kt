package net.ifmain.androiddummy.touch_pattern

import android.app.Application
import android.view.MotionEvent
import androidx.test.core.app.ApplicationProvider
import net.ifmain.androiddummy.testutil.configureDisplayMetrics
import net.ifmain.androiddummy.testutil.singlePointerMotionEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserBehaviorTrackerTest {

    private val application = ApplicationProvider.getApplicationContext<Application>().also {
        configureDisplayMetrics(it, widthPixels = 1080, heightPixels = 2400)
    }

    @Test
    fun `processing enough touches publishes a behavior analysis`() {
        val tracker = UserBehaviorTracker(application)
        val baseTime = System.currentTimeMillis()

        repeat(20) { index ->
            val action = if (index % 2 == 0) MotionEvent.ACTION_DOWN else MotionEvent.ACTION_UP
            tracker.processTouch(
                event = singlePointerMotionEvent(
                    action = action,
                    x = 800f + index,
                    y = 2_000f,
                    downTime = baseTime + (index / 2) * 50L,
                    eventTime = baseTime + index * 25L,
                    pressure = 0.8f,
                    size = 0.35f,
                    touchMajor = 12f,
                    touchMinor = 10f,
                    edgeFlags = MotionEvent.EDGE_RIGHT,
                ),
                velocityX = 200f,
                velocityY = 50f,
            )
        }

        val analysis = tracker.behaviorAnalysis.value
        val summary = tracker.getSessionSummary()

        assertNotNull(analysis)
        assertEquals(20, analysis!!.touchCount)
        assertEquals(20, summary.touchCount)
        assertEquals(analysis, summary.lastAnalysis)
    }

    @Test
    fun `reset clears session state and latest analysis`() {
        val tracker = UserBehaviorTracker(application)
        val baseTime = System.currentTimeMillis()

        repeat(20) { index ->
            tracker.processTouch(
                event = singlePointerMotionEvent(
                    action = MotionEvent.ACTION_DOWN,
                    x = 100f + index,
                    y = 2_000f,
                    downTime = baseTime + index,
                    eventTime = baseTime + index,
                )
            )
        }

        tracker.reset()

        assertNull(tracker.behaviorAnalysis.value)
        assertEquals(0, tracker.getSessionSummary().touchCount)
    }
}
