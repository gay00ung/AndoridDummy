package com.gayoung.microinteractions.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackTypeTest {

    @Test
    fun `combined preserves feedback order`() {
        val combined = FeedbackType.combined(
            FeedbackType.haptic(HapticType.LIGHT),
            FeedbackType.sound(SoundType.TAP),
            FeedbackType.animation(AnimationType.SCALE),
        )

        assertEquals(
            FeedbackType.Combined(
                listOf(
                    FeedbackType.Haptic(HapticType.LIGHT),
                    FeedbackType.Sound(SoundType.TAP),
                    FeedbackType.Animation(AnimationType.SCALE),
                )
            ),
            combined,
        )
    }

    @Test
    fun `custom haptic compares array contents instead of references`() {
        val first = HapticType.CUSTOM(
            pattern = longArrayOf(0, 10, 20),
            amplitudes = intArrayOf(0, 50, 100),
        )
        val second = HapticType.CUSTOM(
            pattern = longArrayOf(0, 10, 20),
            amplitudes = intArrayOf(0, 50, 100),
        )

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `micro interaction custom stores provided name and feedback`() {
        val feedback = FeedbackType.haptic(HapticType.MEDIUM)
        val interaction = MicroInteraction.Custom("custom", feedback)

        assertEquals("custom", interaction.name)
        assertEquals(feedback, interaction.feedback)
        assertTrue(interaction is MicroInteraction.Custom)
    }
}
