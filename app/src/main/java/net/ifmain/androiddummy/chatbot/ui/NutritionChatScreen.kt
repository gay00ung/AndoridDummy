package net.ifmain.androiddummy.chatbot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.ifmain.androiddummy.chatbot.ChatRequest
import net.ifmain.androiddummy.chatbot.ChatbotService
import java.text.SimpleDateFormat
import java.util.*

/**
 * AndroidDummy
 * Class : NutritionChatScreen.
 * Created by gayoung.
 * Created On 2025-07-11.
 * Description:
 */

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionChatScreen(
    onBack: () -> Unit
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // 초기 메시지
    LaunchedEffect(Unit) {
        messages = listOf(
            ChatMessage(
                "안녕하세요! AI 영양 코치입니다 🌟\n배고프거나 식단 관련 고민이 있으시면 편하게 말씀해주세요!",
                false
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 영양 코치") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("뒤로")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 채팅 메시지 목록
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                reverseLayout = false,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message)
                }

                // 로딩 표시
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 입력창
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("메시지를 입력하세요...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp)
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isLoading) {
                                val userMessage = inputText.trim()
                                inputText = ""

                                // 사용자 메시지 추가
                                messages = messages + ChatMessage(userMessage, true)

                                // AI 응답 받기
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val response = ChatbotService.api.sendMessage(
                                            ChatRequest(
                                                message = userMessage,
                                                user_data = mapOf(
                                                    "calories" to 1650,
                                                    "remaining_calories" to 350,
                                                    "protein" to 85
                                                )
                                            )
                                        )

                                        messages = if (response.success && response.response != null) {
                                            messages + ChatMessage(response.response, false)
                                        } else {
                                            messages + ChatMessage(
                                                "죄송해요, 일시적인 오류가 발생했어요. 다시 시도해주세요.",
                                                false
                                            )
                                        }
                                    } catch (_: Exception) {
                                        messages = messages + ChatMessage(
                                            "서버 연결에 실패했어요. 서버가 실행 중인지 확인해주세요.",
                                            false
                                        )
                                    } finally {
                                        isLoading = false
                                        // 스크롤을 최신 메시지로
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "전송",
                            tint = if (inputText.isNotBlank() && !isLoading)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (message.isUser)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(message.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (message.isUser)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}