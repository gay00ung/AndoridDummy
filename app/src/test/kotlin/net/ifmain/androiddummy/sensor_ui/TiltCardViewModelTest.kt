package net.ifmain.androiddummy.sensor_ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.ifmain.androiddummy.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TiltCardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `sensor updates card transforms and applies haptic cooldown`() = runTest(mainDispatcherRule.dispatcher) {
        var now = 2_000L
        val sensorSource = FakeTiltSensorDataSource()
        val haptics = RecordingHaptics()
        val viewModel = TiltCardViewModel(nowMillis = { now })

        viewModel.initSensor(sensorSource, haptics)
        advanceUntilIdle()

        sensorSource.emit(SensorData(x = 3f, y = 4f))
        advanceUntilIdle()

        val firstCard = viewModel.cards.value.first()
        assertEquals(5f, viewModel.tiltIntensity.value, 0.0001f)
        assertEquals(-6f, firstCard.rotation, 0.0001f)
        assertEquals(60f, firstCard.offsetX, 0.0001f)
        assertEquals(15f, firstCard.offsetY, 0.0001f)
        assertEquals(1.1f, firstCard.scale, 0.0001f)
        assertEquals(1, haptics.mediumTapCount)

        now = 2_500L
        sensorSource.emit(SensorData(x = 9f, y = 0f))
        advanceUntilIdle()
        assertEquals(0, haptics.strongTapCount)

        now = 3_501L
        sensorSource.emit(SensorData(x = 9.1f, y = 0f))
        advanceUntilIdle()
        assertEquals(1, haptics.strongTapCount)
    }

    @Test
    fun `sensor lifecycle calls are forwarded`() = runTest(mainDispatcherRule.dispatcher) {
        val sensorSource = FakeTiltSensorDataSource()
        val haptics = RecordingHaptics()
        val viewModel = TiltCardViewModel()

        viewModel.initSensor(sensorSource, haptics)
        advanceUntilIdle()
        viewModel.startSensor()
        viewModel.stopSensor()

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
