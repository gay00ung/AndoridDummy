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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gayoung.microinteractions.core.*
import com.gayoung.microinteractions.extensions.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * AndroidDummy
 * Class : NutritionChatScreen.
 * Created by gayoung.
 * Created On 2025-07-11.
 * Description:
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionChatScreen(
    onBack: () -> Unit,
    viewModel: NutritionChatViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(viewModel, context) {
        viewModel.events.collect { event ->
            val activity = context as? android.app.Activity ?: return@collect

            when (event) {
                NutritionChatEvent.MessageSent -> {
                    activity.window.decorView.triggerMicroInteraction(
                        MicroInteraction.Custom(
                            customName = "message_send",
                            feedback = FeedbackType.combined(
                                FeedbackType.haptic(HapticType.LIGHT),
                                FeedbackType.animation(AnimationType.PULSE)
                            )
                        )
                    )
                }

                NutritionChatEvent.MessageReceived -> {
                    activity.window.decorView.triggerMicroInteraction(
                        MicroInteraction.Custom(
                            customName = "message_received",
                            feedback = FeedbackType.combined(
                                FeedbackType.haptic(HapticType.SELECTION),
                                FeedbackType.animation(AnimationType.BOUNCE)
                            )
                        )
                    )
                }

                NutritionChatEvent.ResponseFailed -> {
                    activity.window.decorView.triggerMicroInteraction(MicroInteraction.Failure)
                }

                NutritionChatEvent.NetworkError -> {
                    activity.window.decorView.triggerMicroInteraction(
                        MicroInteraction.Custom(
                            customName = "network_error",
                            feedback = FeedbackType.haptic(HapticType.ERROR)
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
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
                items(uiState.messages) { message ->
                    ChatBubble(message)
                }

                // 로딩 표시
                if (uiState.isLoading) {
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
                        value = uiState.inputText,
                        onValueChange = viewModel::onInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("메시지를 입력하세요...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp)
                    )

                    IconButton(
                        onClick = viewModel::sendMessage,
                        enabled = uiState.inputText.isNotBlank() && !uiState.isLoading,
                        modifier = Modifier.tapInteraction()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "전송",
                            tint = if (uiState.inputText.isNotBlank() && !uiState.isLoading)
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
    
    // 메시지가 나타날 때 애니메이션
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { if (message.isUser) 50 else -50 }
        )
    ) {
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
            modifier = Modifier
                .widthIn(max = 280.dp)
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
}
