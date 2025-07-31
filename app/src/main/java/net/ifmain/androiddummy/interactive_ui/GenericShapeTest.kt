package net.ifmain.androiddummy.interactive_ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 *
 * @author gayoung.
 * @since 2025. 7. 31.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericShapeTest() {
    val red = Color(0xFFC41001)
    val items = remember { (1..10).map { "${it}번" } }
    val listState = rememberLazyListState()

    val visibleItemsInfo by remember { derivedStateOf { listState.layoutInfo.visibleItemsInfo } }
    val centerY by remember { derivedStateOf { listState.layoutInfo.viewportEndOffset / 2 } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(red)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(items) { index, item ->
                val itemInfo = visibleItemsInfo.find { it.index == index }
                val itemCenterY = itemInfo?.let { (it.offset + it.size / 2) } ?: 0
                val distanceFromCenter = abs(itemCenterY - centerY).toFloat()
                val maxDistance = if (centerY > 0) centerY.toFloat() else 1f
                val normalizedDistance = if (maxDistance > 0f) {
                    (distanceFromCenter / maxDistance).coerceIn(0f, 1f)
                } else {
                    0f
                }

                // 중앙에 가까울수록 1, 멀수록 0
                val scale by animateFloatAsState(
                    targetValue = 1f - (normalizedDistance * 0.2f),
                    animationSpec = tween(300),
                    label = "scale"
                )

                val alpha by animateFloatAsState(
                    targetValue = 1f - (normalizedDistance * 0.5f),
                    animationSpec = tween(300),
                    label = "alpha"
                )

                // Layout을 사용한 커스텀 배치
                Layout(
                    content = {
                        TextBox(
                            text = item,
                            scale = scale,
                            alpha = alpha
                        )
                    }
                ) { measurables, constraints ->
                    val placeable = measurables.first().measure(constraints)

                    layout(placeable.width, placeable.height) {
                        // 중앙에서 멀수록 약간 옆으로 이동
                        val xOffset = (normalizedDistance * 20.dp.toPx()).toInt()
                        placeable.placeRelative(
                            x = if (index % 2 == 0) -xOffset else xOffset,
                            y = 0
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TextBox(
    text: String,
    scale: Float = 1f,
    alpha: Float = 1f
) {
    val density = androidx.compose.ui.platform.LocalDensity.current

    val outerBoxShape = remember(density) {
        GenericShape { size, _ ->
            with(density) {
                moveTo(31.7.dp.toPx(), 3.1.dp.toPx())
                lineTo(size.width, 0f)
                lineTo(size.width - 23.dp.toPx(), size.height)
                lineTo(0f, size.height - 8.dp.toPx())
                close()
            }
        }
    }

    val innerBoxShape = remember(density) {
        GenericShape { size, _ ->
            with(density) {
                moveTo(33.dp.toPx(), 7.7.dp.toPx())
                lineTo(size.width - 13.dp.toPx(), 3.7.dp.toPx())
                lineTo(size.width - 34.dp.toPx(), size.height - 8.dp.toPx())
                lineTo(16.dp.toPx(), size.height - 12.dp.toPx())
                close()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 8.dp)
            .scale(scale)
            .alpha(alpha)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawOutline(
                        outline = outerBoxShape.createOutline(size, layoutDirection, this),
                        color = Color.White
                    )
                    drawOutline(
                        outline = innerBoxShape.createOutline(size, layoutDirection, this),
                        color = Color.Black
                    )
                },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = Color.White
        )
    }
}