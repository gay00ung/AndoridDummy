package net.ifmain.androiddummy.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


/**
 *
 * @author gayoung.
 * @since 2025. 8. 5.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CommonBottomBar(
    onSend: (String) -> Unit,

    ) {
    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var isTyping by remember { mutableStateOf(false) }

    val cloudShape = remember {
        GenericShape { size, _ ->
            val width = size.width
            val height = size.height

            addOval(
                Rect(
                    width * 0.05f, height * 0.2f,
                    width * 0.25f, height * 0.9f
                )
            )
            addOval(
                Rect(
                    width * 0.15f, height * 0.0f,
                    width * 0.3f, height * 0.7f
                )
            )
            addOval(
                Rect(
                    width * 0.15f, height * 0.2f,
                    width * 0.4f, height * 0.95f
                )
            )
            addOval(
                Rect(
                    width * 0.3f, height * 0.0f,
                    width * 0.5f, height * 0.8f
                )
            )
            addOval(
                Rect(
                    width * 0.35f, height * 0.2f,
                    width * 0.6f, height * 0.9f
                )
            )
            addOval(
                Rect(
                    width * 0.5f, height * 0.03f,
                    width * 0.75f, height * 0.75f
                )
            )
            addOval(
                Rect(
                    width * 0.6f, height * 0.2f,
                    width * 0.8f, height * 1f
                )
            )
            addOval(
                Rect(
                    width * 0.7f, height * 0f,
                    width * 0.85f, height * 0.65f
                )
            )
            addOval(
                Rect(
                    width * 0.77f, height * 0.1f,
                    width * 0.99f, height * 0.85f
                )
            )
        }
    }

    BottomAppBar(
        containerColor = Color.White
    ) {
        Column {
            if (isTyping) {
                LinearWavyProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color(0xFF87CEEB),
                    trackColor = Color(0xFFB0E0E6),
                    amplitude = 0.5f,
                    wavelength = 48.dp,
                    waveSpeed = 10.dp
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        isTyping = it.isNotEmpty()
                    },
                    placeholder = { Text("메시지를 입력하세요...") },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = Color(0xFF87CEEB),
                            shape = cloudShape
                        ),
                    singleLine = true,
                    shape = cloudShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSend(inputText)
                            inputText = ""
                            isTyping = false
                            focusManager.clearFocus()
                        }
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "전송",
                        tint = Color(0xFF87CEEB)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CommonBottomBarPreview() {
    CommonBottomBar(
        onSend = { message ->
            // Handle send action
        }
    )
}
