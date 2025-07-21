package net.ifmain.androiddummy.face_rotation.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 *
 * @author gayoung.
 * @since 2025. 7. 21.
 */
@Composable
fun AnimatedFaceGuide(
    modifier: Modifier = Modifier,
    targetPosition: Offset,
    currentPosition: Offset
) {
    val animatedOffset by animateOffsetAsState(
        targetValue = targetPosition,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (currentPosition == targetPosition) 1f else 0.5f,
        animationSpec = tween(500)
    )

    Canvas(modifier = modifier) {
        drawCircle(
            color = Color.Green.copy(alpha = animatedAlpha),
            radius = 100f,
            center = animatedOffset,
            style = Stroke(
                width = 4.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f))
            )
        )
    }
}