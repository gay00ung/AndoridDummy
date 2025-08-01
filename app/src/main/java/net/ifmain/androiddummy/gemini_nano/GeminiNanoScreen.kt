package net.ifmain.androiddummy.gemini_nano

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log

/**
 * Gemini Nano 기능을 테스트하는 화면
 * @author gayoung
 * @since 2025. 8. 1.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiNanoScreen(
    viewModel: GeminiNanoViewModel = viewModel()
) {
    Log.d("GeminiNanoScreen", "화면 진입!")
    
    val uiState by viewModel.uiState.collectAsState()
    val generatedText by viewModel.generatedText.collectAsState()
    
    var promptText by remember { mutableStateOf("") }
    
    Log.d("GeminiNanoScreen", "UI State - isLoading: ${uiState.isLoading}, isModelReady: ${uiState.isModelReady}, error: ${uiState.errorMessage}")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gemini Nano Test") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 모델 상태 표시
            ModelStatusCard(uiState = uiState)
            
            // 프롬프트 입력
            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                label = { Text("프롬프트를 입력하세요") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                enabled = uiState.isModelReady && !uiState.isGenerating
            )
            
            // 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.generateText(promptText) },
                    enabled = uiState.isModelReady && !uiState.isGenerating && promptText.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("텍스트 생성")
                }
                
                Button(
                    onClick = { viewModel.generateTextStream(promptText) },
                    enabled = uiState.isModelReady && !uiState.isGenerating && promptText.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("스트림 생성")
                }
                
                Button(
                    onClick = { viewModel.clearText() },
                    enabled = generatedText.isNotBlank()
                ) {
                    Text("지우기")
                }
            }
            
            // 생성 중 표시
            if (uiState.isGenerating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI가 응답을 생성하고 있습니다...")
                }
            }
            
            // 생성된 텍스트 표시
            if (generatedText.isNotBlank()) {
                GeneratedTextCard(text = generatedText)
            }
            
            // 에러 메시지 표시
            uiState.errorMessage?.let { error ->
                ErrorCard(message = error)
            }
        }
    }
}

@Composable
private fun ModelStatusCard(uiState: GeminiNanoUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                uiState.isLoading -> MaterialTheme.colorScheme.surfaceVariant
                uiState.isModelReady -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                else -> Color(0xFFF44336).copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(12.dp))
                Text("Gemini Nano 모델 초기화 중...")
            } else {
                val statusColor = if (uiState.isModelReady) Color(0xFF4CAF50) else Color(0xFFF44336)
                val statusText = if (uiState.isModelReady) "모델 준비 완료" else "모델 초기화 실패"
                
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(12.dp)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        drawCircle(color = statusColor)
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = statusText,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun GeneratedTextCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "생성된 텍스트",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = Color(0xFFF44336),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}