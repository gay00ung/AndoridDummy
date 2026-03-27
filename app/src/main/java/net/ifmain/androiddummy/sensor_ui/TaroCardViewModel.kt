package net.ifmain.androiddummy.sensor_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.*

class TaroCardViewModel(
    private val nowMillis: () -> Long = System::currentTimeMillis
) : ViewModel() {
    private var tiltSensorManager: TiltSensorDataSource? = null
    private var hapticUtils: HapticFeedbackController? = null

    private val cardEmojis = listOf("🌟", "🌙", "☀️", "⭐", "🔥", "💎", "🌈", "🦋", "🌸", "⚡")

    private val _deckState = MutableStateFlow(
        CardDeckState(
            cards = List(10) { index ->
                TaroCard(
                    id = index,
                    frontImage = cardEmojis[index],
                    backPattern = "🔮"
                )
            }
        )
    )
    val deckState: StateFlow<CardDeckState> = _deckState.asStateFlow()

    private val _tiltDirection = MutableStateFlow(0f) // -1f(왼쪽) ~ 1f(오른쪽)
    val tiltDirection: StateFlow<Float> = _tiltDirection.asStateFlow()

    private var lastFlipTime = 0L
    private val flipCooldown = 800L // 카드 넘김 간격 (ms)

    fun initSensor(
        tiltSensorManager: TiltSensorDataSource,
        hapticUtils: HapticFeedbackController
    ) {
        this.tiltSensorManager = tiltSensorManager
        this.hapticUtils = hapticUtils

        viewModelScope.launch {
            tiltSensorManager.sensorData.collect { sensorData ->
                val tiltY = sensorData.y.coerceIn(-10f, 10f)
                _tiltDirection.value = tiltY / 10f

                // 기울기가 충분히 클 때만 카드 넘김
                val currentTime = nowMillis()
                if (abs(tiltY) > 3f && currentTime - lastFlipTime > flipCooldown) {
                    if (tiltY > 3f) {
                        flipToNext()
                    } else if (tiltY < -3f) {
                        flipToPrevious()
                    }
                    lastFlipTime = currentTime
                    // 원래 햅틱 유지하면서 추가 효과도 가능
                    hapticUtils?.mediumTap()
                }
            }
        }
    }

    private fun flipToNext() {
        val currentState = _deckState.value
        if (currentState.currentTopCardIndex < currentState.cards.size - 1) {
            viewModelScope.launch {
                // 현재 카드 뒤집기 애니메이션
                animateCardFlip(currentState.currentTopCardIndex, true)
                delay(400) // 뒤집기 완료 대기

                // 다음 카드로 이동
                _deckState.value = currentState.copy(
                    currentTopCardIndex = currentState.currentTopCardIndex + 1
                )
            }
        }
    }

    private fun flipToPrevious() {
        val currentState = _deckState.value
        if (currentState.currentTopCardIndex > 0) {
            viewModelScope.launch {
                // 이전 카드로 이동
                _deckState.value = currentState.copy(
                    currentTopCardIndex = currentState.currentTopCardIndex - 1
                )
                delay(100)

                // 현재 카드 앞면으로 뒤집기
                animateCardFlip(currentState.currentTopCardIndex - 1, false)
            }
        }
    }

    private suspend fun animateCardFlip(cardIndex: Int, toBack: Boolean) {
        val steps = 20
        val targetFlip = if (toBack) 1f else 0f

        repeat(steps) { step ->
            val progress = step.toFloat() / steps
            val flipValue = if (toBack) progress else 1f - progress

            val currentState = _deckState.value
            val updatedCards = currentState.cards.mapIndexed { index, card ->
                if (index == cardIndex) {
                    card.copy(flipProgress = flipValue)
                } else card
            }

            _deckState.value = currentState.copy(cards = updatedCards)
            delay(20) // 부드러운 애니메이션
        }
    }

    fun resetDeck() {
        _deckState.value = CardDeckState(
            cards = List(10) { index ->
                TaroCard(
                    id = index,
                    frontImage = cardEmojis[index],
                    backPattern = "🔮",
                    flipProgress = 0f
                )
            },
            currentTopCardIndex = 0
        )
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
