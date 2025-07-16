package net.ifmain.androiddummy.microinteractions

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gayoung.microinteractions.MicroInteractions
import com.gayoung.microinteractions.core.*
import com.gayoung.microinteractions.extensions.*
import com.gayoung.microinteractions.themes.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicroInteractionsShowcaseScreen(
    onBack: () -> Unit
) {
    var selectedTheme by remember { mutableStateOf("Default") }
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    
    // 테마 변경
    LaunchedEffect(selectedTheme) {
        when (selectedTheme) {
            "Default" -> MicroInteractions.applyTheme(DefaultTheme())
            "Subtle" -> MicroInteractions.applyTheme(SubtleTheme())
            "Energetic" -> MicroInteractions.applyTheme(EnergeticTheme())
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MicroInteractions 쇼케이스") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 테마 선택
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "테마 선택",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Default", "Subtle", "Energetic").forEach { theme ->
                                FilterChip(
                                    selected = selectedTheme == theme,
                                    onClick = { selectedTheme = theme },
                                    label = { Text(theme) },
                                    modifier = Modifier.tapInteraction()
                                )
                            }
                        }
                        
                        Text(
                            text = "프리셋 적용",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { MicroInteractions.Presets.applySilent() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("무음", fontSize = 12.sp)
                            }
                            
                            OutlinedButton(
                                onClick = { MicroInteractions.Presets.applyAccessible() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("접근성", fontSize = 12.sp)
                            }
                            
                            OutlinedButton(
                                onClick = { MicroInteractions.Presets.applyBatterySaving() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("배터리 절약", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            
            // 기본 인터랙션
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "기본 인터랙션",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .tapInteraction()
                        ) {
                            Text("Tap")
                        }
                        
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .microInteraction(
                                    MicroInteraction.LongPress,
                                    trigger = ComposeTrigger.OnLongPress
                                )
                        ) {
                            Text("Long Press")
                        }
                    }
                }
            }
            
            // 상태 인터랙션
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "상태 인터랙션",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var isLoading by remember { mutableStateOf(false) }
                    var showSuccess by remember { mutableStateOf(false) }
                    var showFailure by remember { mutableStateOf(false) }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    delay(2000)
                                    isLoading = false
                                    showSuccess = true
                                    delay(1000)
                                    showSuccess = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("성공 시뮬레이션")
                            }
                        }
                        
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    showFailure = true
                                    delay(1000)
                                    showFailure = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("실패 시뮬레이션")
                        }
                    }
                    
                    // 트리거 기반 인터랙션
                    MicroInteractionTrigger(
                        interaction = MicroInteraction.Success,
                        trigger = showSuccess
                    ) {
                        // 빈 컨텐츠
                    }
                    
                    MicroInteractionTrigger(
                        interaction = MicroInteraction.Failure,
                        trigger = showFailure
                    ) {
                        // 빈 컨텐츠
                    }
                }
            }
            
            // 액션 인터랙션
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "액션 인터랙션",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    var isFavorite by remember { mutableStateOf(false) }
                    var isToggled by remember { mutableStateOf(false) }
                    
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 좋아요 버튼
                        IconButton(
                            onClick = { isFavorite = !isFavorite },
                            modifier = Modifier.microInteraction(MicroInteraction.Favorite)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "좋아요",
                                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // 공유 버튼
                        IconButton(
                            onClick = { },
                            modifier = Modifier.microInteraction(MicroInteraction.Share)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "공유")
                        }
                        
                        // 삭제 버튼
                        IconButton(
                            onClick = { },
                            modifier = Modifier.microInteraction(MicroInteraction.Delete)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제")
                        }
                        
                        // 새로고침 버튼
                        IconButton(
                            onClick = { },
                            modifier = Modifier.microInteraction(MicroInteraction.Refresh)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 토글 스위치
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("알림 설정")
                        Switch(
                            checked = isToggled,
                            onCheckedChange = { isToggled = it },
                            modifier = Modifier.toggleInteraction()
                        )
                    }
                }
            }
            
            // 커스텀 인터랙션
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "커스텀 인터랙션",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 무지개 효과 버튼
                    var rainbowColor by remember { mutableStateOf(Color.Red) }
                    val animatedColor by animateColorAsState(targetValue = rainbowColor)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(animatedColor)
                            .clickable {
                                // 직접 View를 사용한 커스텀 인터랙션
                                view.triggerMicroInteraction(
                                    MicroInteraction.Custom(
                                        customName = "rainbow",
                                        feedback = FeedbackType.combined(
                                            FeedbackType.haptic(HapticType.SELECTION),
                                            FeedbackType.animation(AnimationType.ELASTIC)
                                        )
                                    )
                                )
                                
                                // 색상 변경
                                rainbowColor = when (rainbowColor) {
                                    Color.Red -> Color.Yellow
                                    Color.Yellow -> Color.Green
                                    Color.Green -> Color.Blue
                                    Color.Blue -> Color.Magenta
                                    else -> Color.Red
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "무지개 버튼",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            
            // 설정
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "설정",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var hapticEnabled by remember { mutableStateOf(true) }
                    var animationEnabled by remember { mutableStateOf(true) }
                    var intensity by remember { mutableStateOf(0.8f) }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("햅틱 피드백")
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = { 
                                hapticEnabled = it
                                MicroInteractions.configure {
                                    isHapticEnabled = it
                                }
                            }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("애니메이션")
                        Switch(
                            checked = animationEnabled,
                            onCheckedChange = { 
                                animationEnabled = it
                                MicroInteractions.configure {
                                    isAnimationEnabled = it
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("강도: ${(intensity * 100).toInt()}%")
                    Slider(
                        value = intensity,
                        onValueChange = { 
                            intensity = it
                            MicroInteractions.configure {
                                defaultIntensity = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}