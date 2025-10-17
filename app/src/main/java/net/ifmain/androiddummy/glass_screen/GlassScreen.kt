package net.ifmain.androiddummy.glass_screen

import android.annotation.*
import android.content.res.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* -------------------------------------------
 * Theme-ish colors used in this single file.
 * ------------------------------------------- */
private val Black900 = Color(0xFF0B0B0E)
private val TechBlack = Color(0xFF0F1014)
private val White900 = Color(0xFFFFFFFF)
private val Gray400 = Color(0xFFB7BDC8)
private val Cyan100 = Color(0xFF40FFE9)
private val MarketingCardBackground = Color(0xFF12131A)
private val MarketingCardBorder = Color(0x22FFFFFF)

/* -------------------------------------------
 * Shadow data + light-weight shadow modifiers
 * (brush-based, no hardware blur dependency)
 * ------------------------------------------- */

@Immutable
data class Shadow(
    val radius: Dp,
    val offset: DpOffset = DpOffset(0.dp, 0.dp),
    val color: Color = Color.Unspecified,
    val brush: Brush? = null
)

/**
 * Drop shadow by drawing a rounded rect behind content using a brush/color,
 * expanded by [radius] and translated by [offset].
 * This is a light-weight approximation (no real blur).
 */
@SuppressLint("SuspiciousModifierThen")
fun Modifier.dropShadow(
    shape: RoundedCornerShape,
    shadow: Shadow,
): Modifier = graphicsLayer {
    // keep as is; drawing will be handled in drawWithContent below
}.then(
    Modifier.drawWithContent {
        // draw shadow layer FIRST
        drawShadowLayer(
            shape = shape,
            shadow = shadow,
            inner = false
        )
        // then content on top
        drawContent()
    }
)

/**
 * Inner shadow by drawing a shape mask and blending a brush/color inside.
 * Light-weight approximation using clip + blend mode.
 */
@SuppressLint("SuspiciousModifierThen")
fun Modifier.innerShadow(
    shape: RoundedCornerShape,
    shadow: Shadow
): Modifier = graphicsLayer {
    // keep as is; drawing will be handled in drawWithContent below
}.then(
    Modifier.drawWithContent {
        // draw content first
        drawContent()
        // then inner shadow overlay
        drawShadowLayer(
            shape = shape,
            shadow = shadow,
            inner = true
        )
    }
)

private fun DrawScope.drawShadowLayer(
    shape: RoundedCornerShape,
    shadow: Shadow,
    inner: Boolean
) {
    val radiusPx = shadow.radius.toPx()
    val offsetXPx = shadow.offset.x.toPx()
    val offsetYPx = shadow.offset.y.toPx()

    // Target rect: we expand (for drop) or inset (for inner) using radius.
    val baseRect = Rect(0f, 0f, size.width, size.height)
    val targetRect = if (inner) {
        baseRect.inflate(-radiusPx.coerceAtMost(size.minDimension / 2f))
    } else {
        baseRect.inflate(radiusPx)
    }

    val topLeft = Offset(targetRect.left + offsetXPx, targetRect.top + offsetYPx)
    val rectSize = Size(targetRect.width, targetRect.height)

    val corner = shape.topStart.toPx(size, this)
    val cornerRadius = CornerRadius(corner, corner)

    val path = Path().apply {
        addRoundRect(RoundRect(targetRect.translate(offsetXPx, offsetYPx), cornerRadius))
    }

    val brush = shadow.brush ?: shadow.color.let { c ->
        if (c == Color.Unspecified) SolidColor(Color.Black.copy(alpha = 0.25f)) else SolidColor(c)
    }

    if (inner) {
        // Inner: clip to ORIGINAL content bounds, then draw the inset shape with multiply so it looks like inner shade.
        val originalPath = Path().apply {
            addRoundRect(RoundRect(baseRect, cornerRadius))
        }
        clipPath(originalPath) {
            drawPath(
                path = path,
                brush = brush,
                alpha = 1f,
                blendMode = BlendMode.Multiply
            )
        }
    } else {
        // Drop: draw behind content.
        translate(left = 0f, top = 0f) {
            // Fill the rounded rect with given brush/color.
            drawRoundRect(
                brush = brush,
                topLeft = topLeft,
                size = rectSize,
                cornerRadius = cornerRadius,
                alpha = 0.95f
            )
        }
    }
}

/* -------------------------------------------
 * Playground data + composables
 * ------------------------------------------- */

@Immutable
data class ShadowConfig(
    val strokeColor: Color = Color(0xFFFF9718),
    val fillColor: Color = Color(0xFFFF5722),
    val secondaryFillColor: Color = Color(0xFF536DFE),
    val glassReflectColor: Color = Color(0xFFF3DFAC),
    val themeColor: Color = Black900
)

private data class ShadowSample(
    val modifier: Modifier,
    val title: String,
    val description: String,
    val borderColor: Color,
    val icon: ImageVector,
)

/* ---------- Carousel control (in-file) ---------- */
@Composable
private fun CarouselControlButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String = "Navigate",
    modifier: Modifier = Modifier
) {
    val ripple = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0x22FFFFFF))
            .border(1.dp, Color(0x33FFFFFF), CircleShape)
            .clickable(
                interactionSource = ripple,
                indication = null,
                onClick = onClick,
                role = Role.Button
            )
            .semantics {
                this.role = Role.Button
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White
        )
    }
}

/* ---------- Screen ---------- */

@Composable
fun ShadowsScreen() {
    val samples = createShadowSamples()
    var currentIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TechBlack)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val sample = samples[currentIndex]

        ShadowComposable(
            modifier = sample.modifier,
            title = sample.title,
            description = sample.description,
            borderColor = sample.borderColor,
            icon = sample.icon
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CarouselControlButton(
                onClick = {
                    currentIndex =
                        if (currentIndex - 1 < 0) samples.lastIndex else currentIndex - 1
                },
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous sample"
            )
            CarouselControlButton(
                onClick = {
                    currentIndex =
                        if (currentIndex + 1 > samples.lastIndex) 0 else currentIndex + 1
                },
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next sample"
            )
        }
    }
}

/* ---------- Samples ---------- */

@Composable
private fun createShadowSamples(): List<ShadowSample> {
    return listOf(
        createFillShadowSample(),
        createGradientFillShadowSample(),
        createCyanVolumetricShadowSample(
            ShadowConfig(
                strokeColor = Color(0xFF00F5FF),
                fillColor = Color(0xFFFF00FF),
                secondaryFillColor = Color(0xFF9D00FF),
                glassReflectColor = Color(0xFFFFFFFF)
            )
        ),
        createVolumetricShadowSample(
            ShadowConfig(
                strokeColor = Color(0xFF00FF99),
                fillColor = Color(0xFF00FF7F),
                secondaryFillColor = Color(0xFF39FF14),
                glassReflectColor = Color(0xFFFFFFFF),
            )
        ),
        createInnerDropShadowSample(),
    )
}

@Composable
private fun createGradientFillShadowSample(): ShadowSample {
    val config = ShadowConfig(
        strokeColor = Color(0xFFFFD500),
        fillColor = Color(0xFFFF6B35),
        secondaryFillColor = Color(0xFFFFD500)
    )

    val vFill = remember(config) {
        Brush.linearGradient(
            colorStops = arrayOf(
                0.25f to config.fillColor,
                0.75f to config.secondaryFillColor,
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, 1f),
        )
    }

    val modifier = Modifier
        .width(200.dp)
        .wrapContentHeight()
        .dropShadow(
            RoundedCornerShape(42.dp), Shadow(
                radius = 1.dp,
                offset = DpOffset(12.dp, 10.dp),
                color = config.strokeColor,
            )
        )
        .dropShadow(
            RoundedCornerShape(42.dp), Shadow(
                radius = 2.dp,
                offset = DpOffset(8.dp, 6.dp),
                brush = vFill
            )
        )
        .background(config.themeColor, RoundedCornerShape(40.dp))
        .graphicsLayer { clip = true }

    return ShadowSample(
        modifier = modifier,
        title = "Gradient Fill Shadow",
        description = "Linear gradient shadow transitioning from orange to gold for dynamic effects.",
        borderColor = config.strokeColor,
        icon = Icons.Filled.Gradient
    )
}

private fun createFillShadowSample(): ShadowSample {
    val config = ShadowConfig(strokeColor = Color(0xFFFA0505))

    val modifier = Modifier
        .width(200.dp)
        .wrapContentHeight()
        .dropShadow(
            RoundedCornerShape(42.dp), Shadow(
                radius = 1.dp,
                offset = DpOffset(12.dp, 10.dp),
                color = config.strokeColor,
            )
        )
        .dropShadow(
            RoundedCornerShape(38.dp), Shadow(
                radius = 2.dp,
                offset = DpOffset(8.dp, 6.dp),
                color = Color(0xFFFAE0E0),
            )
        )
        .background(config.themeColor, RoundedCornerShape(40.dp))
        .graphicsLayer { clip = true }

    return ShadowSample(
        modifier = modifier,
        title = "Chaining Drop Shadows",
        description = "Chaining two drop shadows with solid color fills to create depth and dimension.",
        borderColor = config.strokeColor,
        icon = Icons.Filled.Layers
    )
}

@Composable
private fun createVolumetricShadowSample(
    config: ShadowConfig,
    title: String = "Volumetric Shadows",
    description: String = "Layering multiple gradient shadows to create realistic 3D depth and dimension.",
    icon: ImageVector = Icons.Filled.AutoAwesome
): ShadowSample {

    val v1 = remember(config) {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.05f to config.strokeColor,
                0.40f to config.strokeColor,
                0.70f to config.glassReflectColor.copy(alpha = 0.5f),
                0.85f to Color.Transparent,
                1f to config.strokeColor.copy(alpha = 0.025f)
            )
        )
    }
    val h1 = remember(config) {
        Brush.horizontalGradient(
            colorStops = arrayOf(
                0.40f to config.strokeColor,
                0.70f to config.glassReflectColor.copy(alpha = 0.5f),
                0.85f to Color.Transparent,
                1f to config.strokeColor.copy(alpha = 0.025f),
            )
        )
    }
    val core = remember(config) {
        Brush.linearGradient(
            colorStops = arrayOf(
                0.35f to config.fillColor,
                0.50f to Color.Black,
                0.75f to Color.Black,
                0.90f to config.fillColor,
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, 1f),
        )
    }

    val modifier = Modifier
        .width(200.dp)
        .wrapContentHeight()
        .dropShadow(
            RoundedCornerShape(40.dp), Shadow(
                radius = 1.dp,
                offset = DpOffset(14.dp, 10.dp),
                brush = v1
            )
        )
        .dropShadow(
            RoundedCornerShape(40.dp), Shadow(
                radius = 1.dp,
                offset = DpOffset(10.dp, 10.dp),
                brush = h1
            )
        )
        .dropShadow(
            RoundedCornerShape(36.dp), Shadow(
                radius = 2.dp,
                offset = DpOffset(8.dp, 6.dp),
                brush = core
            )
        )
        .background(config.themeColor, RoundedCornerShape(40.dp))
        .graphicsLayer { clip = true }

    return ShadowSample(
        modifier = modifier,
        title = title,
        description = description,
        borderColor = config.strokeColor,
        icon = icon
    )
}

@Composable
private fun createCyanVolumetricShadowSample(
    config: ShadowConfig,
    title: String = "Glass Effect?",
    description: String = "Combining multiple gradient shadows with transparent color stops to simulate a glassy effect.",
    icon: ImageVector = Icons.Filled.FilterVintage
): ShadowSample {

    val v1 = remember(config) {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.40f to config.strokeColor,
                0.70f to config.glassReflectColor.copy(alpha = 0.5f),
                0.85f to Color.Transparent,
                1f to config.strokeColor.copy(alpha = 0.025f)
            ),
        )
    }
    val h1 = remember(config) {
        Brush.horizontalGradient(
            colorStops = arrayOf(
                0.40f to config.strokeColor,
                0.70f to config.glassReflectColor.copy(alpha = 0.5f),
                0.85f to Color.Transparent,
                1f to config.strokeColor.copy(alpha = 0.025f),
            )
        )
    }
    val core = remember(config) {
        Brush.linearGradient(
            colorStops = arrayOf(
                0.025f to Color.White,
                0.35f to config.fillColor,
                0.50f to Color.Black,
                0.75f to Color.Black,
                0.90f to config.fillColor,
                0.95f to Color.White,
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, 1f),
        )
    }

    val modifier = Modifier
        .width(200.dp)
        .wrapContentHeight()
        .dropShadow(
            RoundedCornerShape(40.dp), Shadow(
                radius = 1.dp,
                offset = DpOffset(14.dp, 10.dp),
                brush = v1
            )
        )
        .dropShadow(
            RoundedCornerShape(40.dp), Shadow(
                radius = 1.dp,
                offset = DpOffset(10.dp, 10.dp),
                brush = h1
            )
        )
        .dropShadow(
            RoundedCornerShape(36.dp), Shadow(
                radius = 2.dp,
                offset = DpOffset(8.dp, 6.dp),
                brush = core
            )
        )
        .background(config.themeColor, RoundedCornerShape(40.dp))
        .graphicsLayer { clip = true }

    return ShadowSample(
        modifier = modifier,
        title = title,
        description = description,
        borderColor = config.strokeColor,
        icon = icon
    )
}

@Composable
private fun createInnerDropShadowSample(): ShadowSample {
    val config = ShadowConfig(
        strokeColor = Color(0xFF18C9FF),
        fillColor = Color(0xFFFF4081),
        glassReflectColor = Color(0xFF40FFE9),
        secondaryFillColor = Color(0xFF525B5B)
    )

    val v1 = remember(config) {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.40f to config.strokeColor,
                0.70f to config.glassReflectColor.copy(alpha = 0.5f),
                0.85f to config.glassReflectColor.copy(alpha = 0.15f),
                1f to config.strokeColor.copy(alpha = 0.025f)
            ),
        )
    }
    val h1 = remember(config) {
        Brush.horizontalGradient(
            colorStops = arrayOf(
                0.40f to config.strokeColor,
                0.70f to config.glassReflectColor.copy(alpha = 0.5f),
                0.85f to config.glassReflectColor.copy(alpha = 0.15f),
                1f to config.strokeColor.copy(alpha = 0.025f),
            )
        )
    }
    val core = remember(config) {
        Brush.linearGradient(
            colorStops = arrayOf(
                0.05f to Color.White,
                0.15f to config.secondaryFillColor,
                0.35f to config.fillColor,
                0.45f to config.secondaryFillColor,
                0.50f to Color.Black,
                0.75f to Color.Black,
                0.85f to config.fillColor,
                0.90f to config.secondaryFillColor,
                1f to Color.White,
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, 1f),
        )
    }

    val modifier = Modifier
        .width(200.dp)
        .wrapContentHeight()
        .dropShadow(
            RoundedCornerShape(40.dp), Shadow(
                radius = 1.dp,
                offset = DpOffset(14.dp, 10.dp),
                brush = v1
            )
        )
        .dropShadow(
            RoundedCornerShape(40.dp), Shadow(
                radius = 1.dp,
                offset = DpOffset(10.dp, 10.dp),
                brush = h1
            )
        )
        .dropShadow(
            RoundedCornerShape(44.dp), Shadow(
                radius = 2.dp,
                offset = DpOffset(8.dp, 6.dp),
                brush = core
            )
        )
        .background(config.themeColor, RoundedCornerShape(40.dp))
        .innerShadow(
            RoundedCornerShape(42.dp), Shadow(
                radius = 50.dp,
                offset = DpOffset(10.dp, -10.dp),
                brush = Brush.linearGradient(
                    colors = listOf(
                        White900,
                        config.themeColor,
                        Color.Red,
                        config.themeColor,
                    )
                ),
            )
        )
        .innerShadow(
            RoundedCornerShape(42.dp), Shadow(
                radius = 50.dp,
                offset = DpOffset(-10.dp, 10.dp),
                brush = Brush.linearGradient(
                    colors = listOf(
                        config.themeColor,
                        Cyan100,
                        config.themeColor,
                        White900,
                    )
                ),
            )
        )
        .graphicsLayer { clip = true }

    return ShadowSample(
        modifier = modifier,
        title = "Inner + Drop\nShadows",
        description = "Looks like background blur? It's actually inner shadows with high radius values.",
        borderColor = config.strokeColor,
        icon = Icons.Filled.MultipleStop
    )
}

/* ---------- Card body ---------- */

@Composable
fun ShadowComposable(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    borderColor: Color,
    icon: ImageVector = Icons.Outlined.LightMode,
) {
    Box(
        modifier = modifier
            .border(
                width = 4.dp, color = borderColor, shape = RoundedCornerShape(36.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedContent(
                targetState = icon, transitionSpec = {
                    (fadeIn() + slideInHorizontally { it / 2 }) togetherWith (fadeOut() + slideOutHorizontally { -it / 2 })
                }, label = "IconAnimation"
            ) { targetIcon ->
                IconBox(icon = targetIcon)
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = title, transitionSpec = {
                    (fadeIn() + slideInHorizontally { it / 4 }) togetherWith (fadeOut() + slideOutVertically { -it / 4 })
                }, label = "TitleAnimation"
            ) { targetTitle ->
                CardTitle(title = targetTitle)
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = description, transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }, label = "DescriptionAnimation"
            ) { targetDescription ->
                CardDescription(description = targetDescription)
            }
        }
    }
}

@Composable
private fun IconBox(icon: ImageVector) {
    val innerBrush = remember {
        Brush.linearGradient(
            colors = listOf(White900, MarketingCardBackground),
            start = Offset(90f, -90f),
            end = Offset.Zero
        )
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .innerShadow(
                RoundedCornerShape(12.dp), Shadow(
                    12.dp,
                    brush = innerBrush
                )
            )
            .border(
                width = 1.dp, color = MarketingCardBorder, shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Card Icon",
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CardTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 24.sp
    )
}

@Composable
private fun CardDescription(description: String) {
    Text(
        text = description, color = Gray400, fontSize = 14.sp
    )
}

/* ---------- Previews ---------- */

@Preview(
    name = "Light",
    showBackground = true
)
@Composable
fun ShadowsScreenPreviewLight() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(Black900)
                .padding(24.dp)
        ) {
            ShadowsScreen()
        }
    }
}

@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
fun ShadowsScreenPreviewDark() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(Black900)
                .padding(24.dp)
        ) {
            ShadowsScreen()
        }
    }
}
