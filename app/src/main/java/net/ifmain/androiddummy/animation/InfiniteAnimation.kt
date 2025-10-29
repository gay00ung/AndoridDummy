package net.ifmain.androiddummy.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import kotlin.math.*

@Composable
fun InfiniteAnimation(
    duration: Int = 15000,
    size: Dp = 450.dp,
) {
    val emojiList: List<String> = listOf(
        "😀", "😂", "😍", "🤔", "😎", "😭", "😡", "🥳", "😴", "🤖",
        "👻", "💩", "🎃", "🌟", "🔥", "🌈", "🍀", "🌸", "🍎", "⚽",
        "🏀", "🎲", "🚗", "✈️", "🏝", "🌋", "🏔️", "🌌", "🎵", "🎬",
        "📚", "💡", "📱", "💻"
    )

    val shuffledEmojis = remember { emojiList.shuffled() }

    /**
     * Fibonacci sphere: 구 표면에 점들을 균등하게 분포시키는 알고리즘 (점들이 뭉치지 않고 골고루 퍼짐)
     * phi: 위도 각도 (-π/2 ~ π/2, 남극에서 북극까지)
     * acos: 역코사인 함수 (arccos, cos의 역함수)
     * 1 - 2f * (i + 0.5f) / shuffledEmojis.size: -1 ~ 1 사이의 균등한 값 생성
     * theta: 경도 각도, 황금비(1.618...)를 사용하여 나선형으로 분포
     * 각 점의 위치를 (phi, theta) 쌍으로 저장
     */
    val spherePoints = remember {
        List(shuffledEmojis.size) { i ->
            val phi = acos(1 - 2f * (i + 0.5f) / shuffledEmojis.size) - (PI / 2).toFloat()
            val theta = (PI * (1 + sqrt(5.0))).toFloat() * i
            Pair(phi, theta)
        }
    }

    // 수평 드래그 상태 저장
    val hState = remember { mutableFloatStateOf(0f) }

    // 무한 회전 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val baseRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing)
        ),
        label = ""
    )

    // 총 회전량 계산 = 기본 회전 + 드래그에 의한 회전 (자동 재계산)
    val r by remember {
        derivedStateOf {
            baseRotation + hState.floatValue
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            // 수평 드래그 가능
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { d ->
                    // 민감도
                    hState.floatValue += (d * 0.15f)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        spherePoints.forEachIndexed { index, (phi, thetaBase) ->
            // 회전 적용
            // 구의 반지름
            val radius = size.value
            // theta에 회전각(r) 더하기
            val theta = -(thetaBase + Math.toRadians(r.toDouble())).toFloat() % (2 * PI).toFloat()

            // 3D → 2D 투영
            // 구면 좌표계를 직교 좌표계로 변환
            // x -> 좌우 위치, y -> 상하 위치, z -> 깊이
            val x = radius * cos(phi) * cos(theta)
            val y = radius * sin(phi)
            val z = radius * cos(phi) * sin(theta)

            // 원근감 스케일링
            // 뒤쪽은 작게, 앞쪽은 크게
            val scale = 0.3f + 0.7f * ((z / radius + 1f) / 2f)
            val depth = cos(scale)

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        // 이모지의 화면 내 위치 지정
                        translationX = x
                        translationY = y
                        // 크기 조절
                        scaleX = (scale * 1.95f) - depth
                        scaleY = (scale * 1.95f) - depth
                        // 투명도 조절
                        alpha = (scale * 2) - 0.98f
                    }
                    // zIndex로 앞뒤 순서 지정(클 수록 앞에 그림 -> 겹침 문제 해결)
                    .zIndex(z)
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = shuffledEmojis[index],
                    fontSize = 24.sp
                )
            }
        }
    }
}
