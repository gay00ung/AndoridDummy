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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import net.ifmain.androiddummy.sensor_ui.HapticUtils
import net.ifmain.androiddummy.sensor_ui.TaroCard
import net.ifmain.androiddummy.sensor_ui.TaroCardViewModel
import net.ifmain.androiddummy.sensor_ui.TiltSensorManager
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaroCardScreen(
    viewModel: TaroCardViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val deckState by viewModel.deckState.collectAsState()
    val tiltDirection by viewModel.tiltDirection.collectAsState()

    LaunchedEffect(Unit) {
        val sensorManager = TiltSensorManager(context)
        val hapticUtils = HapticUtils(context)
        viewModel.initSensor(sensorManager, hapticUtils)
        viewModel.startSensor()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopSensor() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("타로 카드") },
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
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2D1B69),
                            Color(0xFF11073E)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // 타이틀과 안내
            Column(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔮 타로 카드 덱",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "기기를 좌우로 기울여 카드를 넘겨보세요",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${deckState.currentTopCardIndex + 1} / ${deckState.cards.size}",
                    color = Color.Yellow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 기울기 인디케이터
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 150.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "⬅️",
                    fontSize = 20.sp,
                    color = if (tiltDirection < -0.3f) Color.Yellow else Color.Gray
                )
                Spacer(modifier = Modifier.width(20.dp))
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(6.dp)
                        .background(Color.Gray, RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(6.dp)
                            .offset(x = ((tiltDirection * 46) + 46).dp)
                            .background(Color.Yellow, RoundedCornerShape(3.dp))
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    "➡️",
                    fontSize = 20.sp,
                    color = if (tiltDirection > 0.3f) Color.Yellow else Color.Gray
                )
            }

            // 카드 덱
            Box(
                contentAlignment = Alignment.Center
            ) {
                // 뒤에 깔린 카드들 (스택 효과)
                repeat(3) { stackIndex ->
                    if (deckState.currentTopCardIndex + stackIndex < deckState.cards.size) {
                        Card(
                            modifier = Modifier
                                .size(200.dp, 300.dp)
                                .offset(
                                    x = (stackIndex * 4).dp,
                                    y = (stackIndex * 4).dp
                                )
                                .rotate(stackIndex * 2f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF4A4A4A)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFF6A4C93), Color(0xFF2D1B69))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔮", fontSize = 40.sp)
                            }
                        }
                    }
                }

                // 현재 활성 카드 (뒤집기 효과)
                if (deckState.currentTopCardIndex < deckState.cards.size) {
                    FlippableCard(
                        card = deckState.cards[deckState.currentTopCardIndex],
                        tiltDirection = tiltDirection
                    )
                }
            }

            // 리셋 버튼
            Button(
                onClick = { viewModel.resetDeck() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A4C93))
            ) {
                Text("🔄 덱 리셋", color = Color.White)
            }
        }
    }
}

@Composable
fun FlippableCard(
    card: TaroCard,
    tiltDirection: Float
) {
    Card(
        modifier = Modifier
            .size(200.dp, 300.dp)
            .graphicsLayer {
                rotationY = card.flipProgress * 180f
                rotationZ = tiltDirection * 5f // 기울기에 따른 약간의 회전
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (card.flipProgress < 0.5f) {
                        // 앞면 (뒤집기 전반부)
                        Brush.radialGradient(
                            colors = listOf(Color.White, Color(0xFFF0F0F0))
                        )
                    } else {
                        // 뒤면 (뒤집기 후반부)
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF6A4C93), Color(0xFF2D1B69))
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (card.flipProgress < 0.5f) {
                // 앞면 내용
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = card.frontImage,
                        fontSize = 60.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "카드 ${card.id + 1}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "✨ 행운의 카드 ✨",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            } else {
                // 뒤면 내용
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = card.backPattern,
                        fontSize = 80.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "TARO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}