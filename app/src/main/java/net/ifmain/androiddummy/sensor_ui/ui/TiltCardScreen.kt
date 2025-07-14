package net.ifmain.androiddummy.sensor_ui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import net.ifmain.androiddummy.sensor_ui.TiltCardViewModel
import net.ifmain.androiddummy.sensor_ui.TiltSensorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import net.ifmain.androiddummy.sensor_ui.CardState
import net.ifmain.androiddummy.sensor_ui.HapticUtils
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiltCardScreen(
    viewModel: TiltCardViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val cards by viewModel.cards.collectAsState()
    val tiltIntensity by viewModel.tiltIntensity.collectAsState()

    LaunchedEffect(Unit) {
        val sensorManager = TiltSensorManager(context)
        val hapticUtils = HapticUtils(context)
        viewModel.initSensor(sensorManager, hapticUtils)
        viewModel.startSensor()
    }

    // 앱이 백그라운드로 갈 때 센서 정지
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSensor()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("센서 반응형 UI") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("뒤로")
                    }
                },
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                    )
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1a1a2e),
                            Color.Black
                        ),
                        radius = 800f + (tiltIntensity * 200f)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // 인텐시티 표시기
            Text(
                text = "기울기 강도: ${(tiltIntensity * 10).toInt()}",
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp)
            )

            // 배경 파티클 효과
            repeat(10) { i ->
                val particleOffset by animateFloatAsState(
                    targetValue = tiltIntensity * (i + 1) * 10f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "particle$i"
                )

                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .offset(
                            x = (particleOffset * cos(i * 0.628f)).dp,
                            y = (particleOffset * sin(i * 0.628f)).dp
                        )
                        .background(
                            Color.White.copy(alpha = 0.3f),
                            CircleShape
                        )
                )
            }

            // 멀티 카드들
            cards.forEachIndexed { index, cardState ->
                AnimatedCard(
                    cardState = cardState,
                    index = index,
                    tiltIntensity = tiltIntensity
                )
            }
        }
    }
}

@Composable
fun AnimatedCard(
    cardState: CardState,
    index: Int,
    tiltIntensity: Float
) {
    val animatedRotation by animateFloatAsState(
        targetValue = cardState.rotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "rotation$index"
    )

    val animatedOffsetX by animateFloatAsState(
        targetValue = cardState.offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ), label = "offsetX$index"
    )

    val animatedOffsetY by animateFloatAsState(
        targetValue = cardState.offsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ), label = "offsetY$index"
    )

    val animatedScale by animateFloatAsState(
        targetValue = cardState.scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ), label = "scale$index"
    )

    val cardColors = listOf(
        listOf(Color(0xFF667eea), Color(0xFF764ba2)),
        listOf(Color(0xFFf093fb), Color(0xFFf5576c)),
        listOf(Color(0xFF4facfe), Color(0xFF00f2fe)),
        listOf(Color(0xFF43e97b), Color(0xFF38f9d7)),
        listOf(Color(0xFFfa709a), Color(0xFFfee140))
    )

    Card(
        modifier = Modifier
            .size(280.dp, 180.dp)
            .offset(
                x = animatedOffsetX.dp,
                y = animatedOffsetY.dp + (index * 20).dp
            )
            .rotate(animatedRotation)
            .scale(animatedScale)
            .shadow(
                elevation = (8 + index * 4).dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = cardColors[index][0].copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = (8 + index * 2).dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = cardColors[index],
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(280f, 180f)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎯",
                    fontSize = (24 + index * 2).sp
                )
                Text(
                    text = "Card ${index + 1}",
                    color = Color.White,
                    fontSize = (16 + index).sp
                )
                Text(
                    text = "Tilt: ${tiltIntensity.toInt()}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}