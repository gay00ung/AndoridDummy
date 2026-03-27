package net.ifmain.androiddummy.chatbot.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.ifmain.androiddummy.chatbot.ChatRequest
import net.ifmain.androiddummy.chatbot.ChatResponse
import net.ifmain.androiddummy.chatbot.ChatbotApi
import net.ifmain.androiddummy.chatbot.HealthResponse
import net.ifmain.androiddummy.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionChatViewModelGeneratedTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is stable`() {
        val viewModel = NutritionChatViewModel(chatbotApi = FakeChatbotApi())

        val state = viewModel.uiState.value

        assertEquals(1, state.messages.size)
        assertTrue(state.messages.first().text.contains("AI 영양 코치"))
        assertFalse(state.messages.first().isUser)
        assertEquals("", state.inputText)
        assertFalse(state.isLoading)
    }

    @Test
    fun `blank input is ignored`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = NutritionChatViewModel(chatbotApi = FakeChatbotApi())

        viewModel.onInputChange("   ")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.messages.size)
        assertEquals("   ", viewModel.uiState.value.inputText)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `successful send appends user and bot messages and emits success events`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = NutritionChatViewModel(
            chatbotApi = FakeChatbotApi { request ->
                ChatResponse(
                    success = true,
                    response = "${request.message}에 대한 답변",
                    error = null,
                )
            }
        )
        val events = mutableListOf<NutritionChatEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.take(2).toList(events)
        }

        viewModel.onInputChange(" 사과 먹어도 돼? ")
        viewModel.sendMessage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.inputText)
        assertFalse(state.isLoading)
        assertEquals(3, state.messages.size)
        assertEquals("사과 먹어도 돼?", state.messages[1].text)
        assertTrue(state.messages[1].isUser)
        assertEquals("사과 먹어도 돼?에 대한 답변", state.messages[2].text)
        assertFalse(state.messages[2].isUser)
        assertEquals(
            listOf(
                NutritionChatEvent.MessageSent,
                NutritionChatEvent.MessageReceived,
            ),
            events,
        )
    }

    @Test
    fun `failed response appends fallback message and emits response failed`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = NutritionChatViewModel(
            chatbotApi = FakeChatbotApi {
                ChatResponse(
                    success = false,
                    response = null,
                    error = "temporary",
                )
            }
        )
        val events = mutableListOf<NutritionChatEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.take(2).toList(events)
        }

        viewModel.onInputChange("점심 추천해줘")
        viewModel.sendMessage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(
            "죄송해요, 일시적인 오류가 발생했어요. 다시 시도해주세요.",
            state.messages.last().text,
        )
        assertEquals(
            listOf(
                NutritionChatEvent.MessageSent,
                NutritionChatEvent.ResponseFailed,
            ),
            events,
        )
    }

    @Test
    fun `network exception appends network error message and emits network error`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = NutritionChatViewModel(
            chatbotApi = FakeChatbotApi {
                throw IllegalStateException("boom")
            }
        )
        val events = mutableListOf<NutritionChatEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.take(2).toList(events)
        }

        viewModel.onInputChange("저녁 추천해줘")
        viewModel.sendMessage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(
            "서버 연결에 실패했어요. 서버가 실행 중인지 확인해주세요.",
            state.messages.last().text,
        )
        assertEquals(
            listOf(
                NutritionChatEvent.MessageSent,
                NutritionChatEvent.NetworkError,
            ),
            events,
        )
    }

    private class FakeChatbotApi(
        private val sendMessageHandler: suspend (ChatRequest) -> ChatResponse = {
            ChatResponse(success = true, response = "기본 응답", error = null)
        }
    ) : ChatbotApi {
        override suspend fun sendMessage(request: ChatRequest): ChatResponse {
            return sendMessageHandler(request)
        }

        override suspend fun checkHealth(): HealthResponse {
            return HealthResponse(status = "ok")
        }
    }
}
