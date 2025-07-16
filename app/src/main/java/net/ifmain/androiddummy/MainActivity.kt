package net.ifmain.androiddummy

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gayoung.microinteractions.MicroInteractions
import com.gayoung.microinteractions.core.*
import com.gayoung.microinteractions.extensions.*
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import net.ifmain.androiddummy.biometric.FingerprintAuthScreen
import net.ifmain.androiddummy.biometric.FingerprintAuthTheme
import net.ifmain.androiddummy.chatbot.ui.NutritionChatScreen
import net.ifmain.androiddummy.mlkit.FaceDetectionScreen
import net.ifmain.androiddummy.onnx.ui.AnimeFilterScreen
import net.ifmain.androiddummy.sensor_ui.ui.TaroCardScreen
import net.ifmain.androiddummy.sensor_ui.ui.TiltCardScreen
import net.ifmain.androiddummy.microinteractions.MicroInteractionsShowcaseScreen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // MicroInteractions 초기화
        MicroInteractions.init(this)
        MicroInteractions.configure {
            isHapticEnabled = true
            isSoundEnabled = false // 소리는 끄고 햅틱만 사용
            isAnimationEnabled = true
            defaultIntensity = 0.8f
        }
        setContent {
            FingerprintAuthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onFingerprintClick = { navController.navigate("fingerprint") },
                onFaceDetectionClick = { navController.navigate("face_detection") },
                onAnimeFilterClick = { navController.navigate("anime_filter") },
                onNutritionChatClick = { navController.navigate("nutrition_chat") },
                onSensorUiClick = { navController.navigate("sensor_ui") },
                onTaroCardClick = { navController.navigate("taro_card") },
                onMicroInteractionsClick = { navController.navigate("microinteractions") }
            )
        }
        composable("fingerprint") {
            FingerprintAuthScreen()
        }
        composable("face_detection") {
            FaceDetectionScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("anime_filter") {
            AnimeFilterScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("nutrition_chat") {
            NutritionChatScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("sensor_ui") {
            TiltCardScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("taro_card") {
            TaroCardScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("microinteractions") {
            MicroInteractionsShowcaseScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onFingerprintClick: () -> Unit,
    onFaceDetectionClick: () -> Unit,
    onAnimeFilterClick: () -> Unit,
    onNutritionChatClick: () -> Unit,
    onSensorUiClick: () -> Unit,
    onTaroCardClick: () -> Unit,
    onMicroInteractionsClick: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Android Dummy") },
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                    )
                )
            )
        },
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onFingerprintClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .microInteraction(MicroInteraction.Tap)
            ) {
                Text("지문 인증 테스트")
            }

            Button(
                onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        onFaceDetectionClick()
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .successInteraction() // 성공 효과
            ) {
                Text("얼굴 표정 인식")
            }

            Button(
                onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        onAnimeFilterClick()
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .microInteraction(MicroInteraction.Toggle) // 토글 효과
            ) {
                Text("애니메이션 필터")
            }

            Button(
                onClick = onNutritionChatClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .microInteraction(
                        MicroInteraction.Custom(
                            customName = "chat",
                            feedback = com.gayoung.microinteractions.core.FeedbackType.combined(
                                com.gayoung.microinteractions.core.FeedbackType.haptic(com.gayoung.microinteractions.core.HapticType.LIGHT),
                                com.gayoung.microinteractions.core.FeedbackType.animation(com.gayoung.microinteractions.core.AnimationType.PULSE)
                            )
                        )
                    )
            ) {
                Text("AI 영양 코치")
            }

            Button(
                onClick = onSensorUiClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .microInteraction(
                        interaction = MicroInteraction.Refresh,
                        trigger = com.gayoung.microinteractions.extensions.ComposeTrigger.OnClick
                    )
            ) {
                Text("센서 반응형 UI")
            }

            Button(
                onClick = onTaroCardClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .microInteraction(MicroInteraction.Favorite) // 좋아요 효과
            ) {
                Text("타로 카드")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // MicroInteractions 쇼케이스 버튼
            OutlinedButton(
                onClick = onMicroInteractionsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .microInteraction(
                        MicroInteraction.Custom(
                            customName = "showcase",
                            feedback = FeedbackType.combined(
                                FeedbackType.haptic(HapticType.MEDIUM),
                                FeedbackType.animation(AnimationType.ELASTIC)
                            )
                        )
                    ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("MicroInteractions 쇼케이스")
            }

            if (!cameraPermissionState.status.isGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "카메라 권한이 필요합니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}