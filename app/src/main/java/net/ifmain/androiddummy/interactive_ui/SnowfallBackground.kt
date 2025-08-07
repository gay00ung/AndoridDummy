package net.ifmain.androiddummy.interactive_ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

/**
 *
 * @author gayoung.
 * @since 2025. 8. 7.
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun SnowfallBackground() {
    val snowflakes = remember { mutableStateListOf<Snowflake>() }

    val infiniteTransition = rememberInfiniteTransition()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val density = LocalDensity.current
    val screenWidthPx = with(density) { screenWidth.toPx() }
    val screenHeightPx = with(density) { screenHeight.toPx() }

    val animationTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // 바람 효과
    val windStrength by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )


    LaunchedEffect(Unit) {
        // 초기 눈송이 생성
        repeat(50) {
            snowflakes.add(
                Snowflake(
                    x = Random.nextFloat() * screenWidthPx,
                    y = Random.nextFloat() * screenHeightPx,
                    size = Random.nextFloat() * 8f + 4f,
                    speed = Random.nextFloat() * 2f + 1f,
                    alpha = Random.nextFloat() * 0.7f + 0.3f
                )
            )
        }
    }

    // 애니메이션 업데이트
    LaunchedEffect(animationTime) {
        snowflakes.forEachIndexed { index, snowflake ->
            snowflake.y += snowflake.speed
            snowflake.x += sin(snowflake.y * 0.01f) * 0.5f // 좌우 흔들림

            // 화면 밖으로 나가면 위로 리셋
            if (snowflake.y > screenHeightPx) {
                snowflake.y = -20f
                snowflake.x = Random.nextFloat() * screenWidthPx
            }
        }
    }

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        snowflakes.forEach { snowflake ->
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = Color.WHITE
                    alpha = (snowflake.alpha * 255).toInt()
                    textSize = snowflake.size * 2
                }
                canvas.nativeCanvas.drawText(
                    "❄️",
                    snowflake.x,
                    snowflake.y,
                    paint
                )
            }
        }
    }
}

data class Snowflake(
    var x: Float,
    var y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)