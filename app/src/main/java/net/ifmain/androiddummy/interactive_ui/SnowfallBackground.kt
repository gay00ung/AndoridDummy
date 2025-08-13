package net.ifmain.androiddummy.interactive_ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
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
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
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

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // animationTime을 사용하여 실시간으로 위치 계산
        snowflakes.forEachIndexed { index, snowflake ->
            val currentY = (snowflake.y + animationTime * snowflake.speed * 0.1f) % (screenHeightPx + 50f)
            val currentX = snowflake.x + sin(currentY * 0.01f) * 10f
            
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = Color.WHITE
                    alpha = (snowflake.alpha * 255).toInt()
                    textSize = snowflake.size * 2
                }
                canvas.nativeCanvas.drawText(
                    "❄️",
                    currentX,
                    currentY,
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