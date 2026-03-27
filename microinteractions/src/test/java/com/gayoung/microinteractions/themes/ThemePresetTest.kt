package com.gayoung.microinteractions.themes

import androidx.compose.ui.graphics.Color
import com.gayoung.microinteractions.core.AnimationType
import com.gayoung.microinteractions.core.FeedbackType
import com.gayoung.microinteractions.core.HapticType
import com.gayoung.microinteractions.core.SoundType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePresetTest {

    @Test
    fun `default theme exposes balanced tap feedback`() {
        val theme = DefaultTheme()

        assertEquals(
            FeedbackType.Combined(
                listOf(
                    FeedbackType.Haptic(HapticType.LIGHT),
                    FeedbackType.Sound(SoundType.TAP),
                    FeedbackType.Animation(AnimationType.SCALE),
                )
            ),
            theme.tapFeedback,
        )
        assertEquals(Color(0xFF4CAF50), theme.successColor)
        assertEquals(0.3f, theme.animationDuration, 0.0001f)
        assertEquals(0.7f, theme.defaultIntensity, 0.0001f)
    }

    @Test
    fun `subtle and energetic themes tune interaction intensity differently`() {
        val subtle = SubtleTheme()
        val energetic = EnergeticTheme()

        assertEquals(FeedbackType.Haptic(HapticType.SELECTION), subtle.toggleFeedback)
        assertEquals(0.5f, subtle.defaultIntensity, 0.0001f)
        assertEquals(0.9f, energetic.defaultIntensity, 0.0001f)
        assertEquals(0.25f, energetic.animationDuration, 0.0001f)
        assertTrue(energetic.tapFeedback is FeedbackType.Combined)
    }
}
