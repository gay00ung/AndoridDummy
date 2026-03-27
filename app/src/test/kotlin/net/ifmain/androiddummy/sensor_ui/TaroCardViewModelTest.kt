package net.ifmain.androiddummy.sensor_ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.ifmain.androiddummy.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaroCardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `positive and negative tilt navigate the deck with cooldown`() = runTest(mainDispatcherRule.dispatcher) {
        var now = 1_000L
        val sensorSource = FakeTiltSensorDataSource()
        val haptics = RecordingHaptics()
        val viewModel = TaroCardViewModel(nowMillis = { now })

        viewModel.initSensor(sensorSource, haptics)
        advanceUntilIdle()

        sensorSource.emit(SensorData(y = 5f))
        advanceUntilIdle()

        assertEquals(1, viewModel.deckState.value.currentTopCardIndex)
        assertEquals(0.5f, viewModel.tiltDirection.value, 0.0001f)
        assertEquals(1, haptics.mediumTapCount)

        now = 2_500L
        sensorSource.emit(SensorData(y = -6f))
        advanceUntilIdle()

        assertEquals(0, viewModel.deckState.value.currentTopCardIndex)
        assertEquals(-0.6f, viewModel.tiltDirection.value, 0.0001f)
        assertEquals(2, haptics.mediumTapCount)
    }

    @Test
    fun `resetDeck and sensor lifecycle delegate correctly`() = runTest(mainDispatcherRule.dispatcher) {
        val sensorSource = FakeTiltSensorDataSource()
        val haptics = RecordingHaptics()
        val viewModel = TaroCardViewModel()

        viewModel.initSensor(sensorSource, haptics)
        advanceUntilIdle()
        viewModel.startSensor()
        viewModel.stopSensor()
        viewModel.resetDeck()

        assertEquals(0, viewModel.deckState.value.currentTopCardIndex)
        assertTrue(viewModel.deckState.value.cards.all { it.flipProgress == 0f })
        assertEquals(1, sensorSource.startListeningCalls)
        assertEquals(1, sensorSource.stopListeningCalls)
    }

    private class FakeTiltSensorDataSource(
        initial: SensorData = SensorData()
    ) : TiltSensorDataSource {
        private val mutableState = MutableStateFlow(initial)
        override val sensorData: StateFlow<SensorData> = mutableState
        var startListeningCalls = 0
        var stopListeningCalls = 0

        override fun startListening() {
            startListeningCalls++
        }

        override fun stopListening() {
            stopListeningCalls++
        }

        fun emit(sensorData: SensorData) {
            mutableState.value = sensorData
        }
    }

    private class RecordingHaptics : HapticFeedbackController {
        var lightTapCount = 0
        var mediumTapCount = 0
        var strongTapCount = 0

        override fun lightTap() {
            lightTapCount++
        }

        override fun mediumTap() {
            mediumTapCount++
        }

        override fun strongTap() {
            strongTapCount++
        }
    }
}
