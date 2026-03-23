package net.ifmain.androiddummy.age

import androidx.compose.material3.*
import androidx.compose.runtime.*
import net.ifmain.androiddummy.component.*

@Composable
fun InferenceAgeResultScreen(
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            CommonTopBar(
                title = "나이 추론 결과",
                onBack = onBack
            )

        },
        content = { innerPadding ->

        }
    )
}
