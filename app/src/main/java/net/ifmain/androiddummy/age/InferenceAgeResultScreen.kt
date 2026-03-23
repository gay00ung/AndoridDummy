package net.ifmain.androiddummy.age

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import net.ifmain.androiddummy.component.*

@Composable
fun InferenceAgeResultScreen(
    viewModel: InferenceAgeViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.result

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "추론 결과",
                onBack = onBack
            )

        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.clearResult()
                        onBack()
                    },
                ) {
                    Text(text = "다시 시도하기")
                }
            }
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (result == null) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                        )
                        Text(text = "추론 결과가 없습니다.")
                    } else {
                        Image(
                            bitmap = result.faceBitmap.asImageBitmap(),
                            contentDescription = "Face",
                            modifier = Modifier.size(200.dp)
                        )
                        Text(text = "나이: ${result.age}")
                        Text(text = "성별: ${result.gender}")
                    }
                }
            }
        }
    )
}
