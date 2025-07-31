package net.ifmain.androiddummy

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import net.ifmain.androiddummy.touch_pattern.TouchMonitoringScreen
import net.ifmain.androiddummy.face_rotation.ui.FaceVerificationScreen
import net.ifmain.androiddummy.interactive_ui.GenericShapeTest

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
                onMicroInteractionsClick = { navController.navigate("microinteractions") },
                onTouchMonitoringClick = { navController.navigate("touch_monitoring") },
                onFaceVerificationClick = { navController.navigate("face_verification") },
                onInteractiveUiClick = { navController.navigate("interactive_ui") }
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
        composable("touch_monitoring") {
            TouchMonitoringScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("face_verification") {
            FaceVerificationScreen(
                onVerificationComplete = { navController.popBackStack() }
            )
        }
        composable("interactive_ui") {
            GenericShapeTest()
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
    onMicroInteractionsClick: () -> Unit,
    onTouchMonitoringClick: () -> Unit,
    onFaceVerificationClick: () -> Unit,
    onInteractiveUiClick: () -> Unit
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                            feedback = FeedbackType.combined(
                                FeedbackType.haptic(HapticType.LIGHT),
                                FeedbackType.animation(AnimationType.PULSE)
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
                        trigger = ComposeTrigger.OnClick
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
            
            // 터치 모니터링 버튼
            Button(
                onClick = onTouchMonitoringClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .microInteraction(MicroInteraction.Tap),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("터치 데이터 실시간 모니터링")
            }
            
            // 얼굴 회전 인증 버튼
            Button(
                onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        onFaceVerificationClick()
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .microInteraction(
                        MicroInteraction.Custom(
                            customName = "face_verify",
                            feedback = FeedbackType.combined(
                                FeedbackType.haptic(HapticType.HEAVY),
                                FeedbackType.animation(AnimationType.SCALE)
                            )
                        )
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("얼굴 회전 인증")
            }

            // Interactive UI 테스트 버튼
            Button(
                onClick = onInteractiveUiClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .microInteraction(
                        MicroInteraction.Custom(
                            customName = "face_verify",
                            feedback = FeedbackType.combined(
                                FeedbackType.haptic(HapticType.HEAVY),
                                FeedbackType.animation(AnimationType.SCALE)
                            )
                        )
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Interactive UI 테스트")
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