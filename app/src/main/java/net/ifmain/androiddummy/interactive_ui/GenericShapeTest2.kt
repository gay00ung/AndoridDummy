package net.ifmain.androiddummy.interactive_ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import net.ifmain.androiddummy.component.CommonBottomBar
import net.ifmain.androiddummy.component.CommonTopBar
import kotlin.math.abs

/**
 *
 * @author gayoung.
 * @since 2025. 8. 5.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericShapeTest2(
    onBack: () -> Unit
) {
    val red = Color(0xFFC41001)
    val items = remember { mutableStateListOf<String>() }
    var itemCounter by remember { mutableIntStateOf(1) }

    val listState = rememberLazyListState()
    var isAutoScrolling by remember { mutableStateOf(false) }

    val visibleItemsInfo by remember { derivedStateOf { listState.layoutInfo.visibleItemsInfo } }
    val centerY by remember { derivedStateOf { listState.layoutInfo.viewportEndOffset / 2 } }

    val density = LocalDensity.current
    val canFitOnScreen = with(density) {
        listState.layoutInfo.viewportSize.height / 116.dp.toPx()
    }

    var message by remember { mutableStateOf("") }

    LaunchedEffect(isAutoScrolling) {
        if (isAutoScrolling) {
            while (isAutoScrolling) {
                delay(500)
                items.add("${itemCounter}번")
                itemCounter++

                if (items.size > canFitOnScreen) {
                    listState.animateScrollToItem(items.size - 1)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Generic Shape Test",
                onBack = { onBack }
            )
        },
        bottomBar = {
            CommonBottomBar(
                onSend = { message ->
                    items.add(message)
                },
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(red)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
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

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .clickable { isAutoScrolling = !isAutoScrolling },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAutoScrolling) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isAutoScrolling) "일시정지" else "자동재생",
                        tint = red,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    )
}