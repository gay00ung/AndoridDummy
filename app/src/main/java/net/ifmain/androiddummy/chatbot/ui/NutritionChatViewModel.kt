package net.ifmain.androiddummy.chatbot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.ifmain.androiddummy.chatbot.ChatRequest
import net.ifmain.androiddummy.chatbot.ChatbotApi
import net.ifmain.androiddummy.chatbot.ChatbotService

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class NutritionChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "안녕하세요! AI 영양 코치입니다 🌟\n배고프거나 식단 관련 고민이 있으시면 편하게 말씀해주세요!",
            isUser = false
        )
    ),
    val inputText: String = "",
    val isLoading: Boolean = false,
)

sealed interface NutritionChatEvent {
    data object MessageSent : NutritionChatEvent
    data object MessageReceived : NutritionChatEvent
    data object ResponseFailed : NutritionChatEvent
    data object NetworkError : NutritionChatEvent
}

class NutritionChatViewModel(
    private val chatbotApi: ChatbotApi = ChatbotService.api
) : ViewModel() {

    private val defaultUserData = mapOf(
        "calories" to 1650,
        "remaining_calories" to 350,
        "protein" to 85,
    )

    private val _uiState = MutableStateFlow(NutritionChatUiState())
    val uiState: StateFlow<NutritionChatUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<NutritionChatEvent>()
    val events: SharedFlow<NutritionChatEvent> = _events.asSharedFlow()

    fun onInputChange(input: String) {
        _uiState.update { it.copy(inputText = input) }
    }

    fun sendMessage() {
        val currentState = _uiState.value
        val userMessage = currentState.inputText.trim()

        if (userMessage.isBlank() || currentState.isLoading) {
            return
        }

        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage(text = userMessage, isUser = true),
                inputText = "",
                isLoading = true,
            )
        }

        viewModelScope.launch {
            _events.emit(NutritionChatEvent.MessageSent)

            runCatching {
                chatbotApi.sendMessage(
                    ChatRequest(
                        message = userMessage,
                        user_data = defaultUserData,
                    )
                )
            }.onSuccess { response ->
                if (response.success && response.response != null) {
                    _uiState.update {
                        it.copy(
                            messages = it.messages + ChatMessage(
                                text = response.response,
                                isUser = false,
                            ),
                            isLoading = false,
                        )
                    }
                    _events.emit(NutritionChatEvent.MessageReceived)
                } else {
                    _uiState.update {
                        it.copy(
                            messages = it.messages + ChatMessage(
                                text = "죄송해요, 일시적인 오류가 발생했어요. 다시 시도해주세요.",
                                isUser = false,
                            ),
                            isLoading = false,
                        )
                    }
                    _events.emit(NutritionChatEvent.ResponseFailed)
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        messages = it.messages + ChatMessage(
                            text = "서버 연결에 실패했어요. 서버가 실행 중인지 확인해주세요.",
                            isUser = false,
                        ),
                        isLoading = false,
                    )
                }
                _events.emit(NutritionChatEvent.NetworkError)
            }
        }
    }
}
