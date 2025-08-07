package net.ifmain.androiddummy.interactive_ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val items = remember { mutableStateListOf<String>() }

    val listState = rememberLazyListState()

    val visibleItemsInfo by remember { derivedStateOf { listState.layoutInfo.visibleItemsInfo } }
    val centerY by remember { derivedStateOf { listState.layoutInfo.viewportEndOffset / 2 } }

    val density = LocalDensity.current
    val canFitOnScreen = with(density) {
        listState.layoutInfo.viewportSize.height / 116.dp.toPx()
    }

    LaunchedEffect(canFitOnScreen) {
        if (items.size > canFitOnScreen) {
            listState.animateScrollToItem(items.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "",
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
                    .background(Color.White)
            ) {
                SnowfallBackground()

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
                                CloudTextBox(
                                    text = item,
                                    scale = scale,
                                    alpha = alpha,
                                    isLeft = index % 2 == 0
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
    )
}

@Composable
fun CloudTextBox(
    text: String,
    scale: Float = 1f,
    alpha: Float = 1f,
    isAnimated: Boolean = true,
    isLeft: Boolean = true
) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition()

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isAnimated) 8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // 왼쪽 말풍선 모양
    val outerCloudShape = remember(density) {
        GenericShape { size, _ ->
            with(density) {
                val width = size.width
                val height = size.height

                addOval(
                    Rect(
                        width * 0.1f, height * 0.1f,
                        width * 0.9f, height * 0.7f
                    )
                )

                // 왼쪽 봉우리
                addOval(
                    Rect(
                        width * 0.0f, height * 0.2f,
                        width * 0.4f, height * 0.6f
                    )
                )

                // 오른쪽 봉우리
                addOval(
                    Rect(
                        width * 0.6f, height * 0.2f,
                        width * 1.0f, height * 0.6f
                    )
                )

                // 상단 왼쪽
                addOval(
                    Rect(
                        width * 0.15f, height * 0.05f,
                        width * 0.4f, height * 0.3f
                    )
                )

                // 상단 중앙
                addOval(
                    Rect(
                        width * 0.35f, height * 0.0f,
                        width * 0.65f, height * 0.25f
                    )
                )

                // 상단 오른쪽
                addOval(
                    Rect(
                        width * 0.6f, height * 0.05f,
                        width * 0.85f, height * 0.3f
                    )
                )

                // 왼쪽 꼬리만 정의
                val tailStartX = width * 0.15f
                val tailStartY = height * 0.55f
                val tailPointX = width * 0.05f
                val tailPointY = height * 0.85f
                val tailEndX = width * 0.3f
                val tailEndY = height * 0.6f

                moveTo(tailStartX, tailStartY)
                quadraticTo(
                    tailStartX - width * 0.03f, tailPointY - height * 0.1f,
                    tailPointX, tailPointY
                )
                quadraticTo(
                    tailEndX - width * 0.08f, tailPointY - height * 0.08f,
                    tailEndX, tailEndY
                )
                close()
            }
        }
    }

    val innerCloudShape = remember(density) {
        GenericShape { size, _ ->
            with(density) {
                val width = size.width
                val height = size.height

                // 내부 구름 (왼쪽 모양만)
                addOval(
                    Rect(
                        width * 0.13f, height * 0.13f,
                        width * 0.87f, height * 0.67f
                    )
                )

                addOval(
                    Rect(
                        width * 0.03f, height * 0.23f,
                        width * 0.37f, height * 0.57f
                    )
                )

                addOval(
                    Rect(
                        width * 0.63f, height * 0.23f,
                        width * 0.97f, height * 0.57f
                    )
                )

                addOval(
                    Rect(
                        width * 0.18f, height * 0.08f,
                        width * 0.37f, height * 0.27f
                    )
                )

                addOval(
                    Rect(
                        width * 0.38f, height * 0.03f,
                        width * 0.62f, height * 0.22f
                    )
                )

                addOval(
                    Rect(
                        width * 0.63f, height * 0.08f,
                        width * 0.82f, height * 0.27f
                    )
                )

                // 왼쪽 내부 꼬리만
                val tailStartX = width * 0.18f
                val tailStartY = height * 0.52f
                val tailPointX = width * 0.08f
                val tailPointY = height * 0.82f
                val tailEndX = width * 0.27f
                val tailEndY = height * 0.57f

                moveTo(tailStartX, tailStartY)
                quadraticTo(
                    tailStartX - width * 0.02f, tailPointY - height * 0.08f,
                    tailPointX, tailPointY
                )
                quadraticTo(
                    tailEndX - width * 0.06f, tailPointY - height * 0.06f,
                    tailEndX, tailEndY
                )
                close()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .offset(y = floatingOffset.dp)
            .scale(scale)
            .alpha(alpha)
            // 오른쪽 말풍선은 가로축 뒤집기
            .then(
                if (!isLeft) Modifier.scale(scaleX = -1f, scaleY = 1f)
                else Modifier
            )
    ) {
        // 그림자 효과
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 2.dp, y = 4.dp)
                .drawBehind {
                    drawOutline(
                        outline = outerCloudShape.createOutline(size, layoutDirection, this),
                        color = Color(0x40000000)
                    )
                }
        )

        // 메인 구름
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // 외부 구름 (그라데이션)
                    drawOutline(
                        outline = outerCloudShape.createOutline(size, layoutDirection, this),
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF87CEEB),
                                Color(0xFF4682B4)
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.3f),
                            radius = size.width * 0.6f
                        )
                    )

                    // 내부 구름 (그라데이션)
                    drawOutline(
                        outline = innerCloudShape.createOutline(size, layoutDirection, this),
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFF0F8FF)
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.25f),
                            radius = size.width * 0.4f
                        )
                    )

                    // 하이라이트 효과
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x60FFFFFF),
                                Color(0x00FFFFFF)
                            ),
                            radius = size.width * 0.15f
                        ),
                        radius = size.width * 0.1f,
                        center = Offset(size.width * 0.4f, size.height * 0.15f)
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x40FFFFFF),
                                Color(0x00FFFFFF)
                            ),
                            radius = size.width * 0.1f
                        ),
                        radius = size.width * 0.06f,
                        center = Offset(size.width * 0.65f, size.height * 0.18f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = Color(0xFF2C3E50),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .then(
                        if (!isLeft) Modifier.scale(scaleX = -1f, scaleY = 1f)
                        else Modifier
                    )
            )
        }
    }
}