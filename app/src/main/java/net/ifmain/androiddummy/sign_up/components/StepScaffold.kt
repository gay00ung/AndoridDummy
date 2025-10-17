package net.ifmain.androiddummy.sign_up.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepScaffold(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
    leftBtn: Pair<String, () -> Unit>?,
    rightBtnLabel: String,
    onRight: () -> Unit
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(title) }) }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.fillMaxWidth(), content = content)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (leftBtn != null) {
                    OutlinedButton(onClick = leftBtn.second) { Text(leftBtn.first) }
                } else {
                    Spacer(Modifier)
                }
                Button(onClick = onRight) { Text(rightBtnLabel) }
            }
        }
    }
}
