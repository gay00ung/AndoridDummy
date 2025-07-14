package net.ifmain.androiddummy.sensor_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

class TiltCardViewModel : ViewModel() {
    private var tiltSensorManager: TiltSensorManager? = null
    private var hapticUtils: HapticUtils? = null

    private val _cards = MutableStateFlow(
        List(5) { index ->
            CardState(
                id = index,
                rotation = 0f,
                offsetX = 0f,
                offsetY = 0f,
                scale = 1f
            )
        }
    )
    val cards: StateFlow<List<CardState>> = _cards.asStateFlow()

    private val _tiltIntensity = MutableStateFlow(0f)
    val tiltIntensity: StateFlow<Float> = _tiltIntensity.asStateFlow()

    private var lastHapticTime = 0L
    private var lastIntensityLevel = 0

    fun initSensor(tiltSensorManager: TiltSensorManager, hapticUtils: HapticUtils) {
        this.tiltSensorManager = tiltSensorManager
        this.hapticUtils = hapticUtils

        viewModelScope.launch {
            tiltSensorManager.sensorData.collect { sensorData ->
                val intensity = sqrt(sensorData.x * sensorData.x + sensorData.y * sensorData.y)
                _tiltIntensity.value = intensity

                // 햅틱 피드백 (강도별로 다른 진동)
                handleHapticFeedback(intensity)

                val newCards = _cards.value.mapIndexed { index, card ->
                    val dampening = 1f - (index * 0.15f)

                    card.copy(
                        rotation = (sensorData.x * -2f * dampening).coerceIn(-25f, 25f),
                        offsetX = (sensorData.y * 15f * dampening).coerceIn(-150f, 150f),
                        offsetY = (sensorData.x * 5f * dampening).coerceIn(-50f, 50f),
                        scale = 1f + (intensity * 0.02f * dampening).coerceIn(0f, 0.2f)
                    )
                }
                _cards.value = newCards
            }
        }
    }

    private fun handleHapticFeedback(intensity: Float) {
        val currentTime = System.currentTimeMillis()
        val intensityLevel = when {
            intensity > 8f -> 3 // 강한 기울기
            intensity > 4f -> 2 // 중간 기울기
            intensity > 2f -> 1 // 약한 기울기
            else -> 0
        }

        // 1초에 한 번씩만 햅틱 피드백 (너무 자주 진동하지 않게)
        if (currentTime - lastHapticTime > 1000 &&
            intensityLevel > lastIntensityLevel) {

            when (intensityLevel) {
                1 -> hapticUtils?.lightTap()
                2 -> hapticUtils?.mediumTap()
                3 -> hapticUtils?.strongTap()
            }

            lastHapticTime = currentTime
        }

        lastIntensityLevel = intensityLevel
    }

    fun startSensor() {
        tiltSensorManager?.startListening()
    }

    fun stopSensor() {
        tiltSensorManager?.stopListening()
    }

    override fun onCleared() {
        super.onCleared()
        stopSensor()
    }
}

data class CardState(
    val id: Int,
    val rotation: Float,
    val offsetX: Float,
    val offsetY: Float,
    val scale: Float
)