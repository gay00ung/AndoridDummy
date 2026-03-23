package net.ifmain.androiddummy.age

import android.graphics.*
import androidx.activity.compose.*
import androidx.activity.result.*
import androidx.activity.result.contract.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import com.google.accompanist.permissions.*
import net.ifmain.androiddummy.component.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun InferenceAgeScreen(
    viewModel: InferenceAgeViewModel,
    onBack: () -> Unit,
    onNavigateToResult: () -> Unit,
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()

    var showSourceDialog by remember { mutableStateOf(false) }
    var useCameraMode by remember { mutableStateOf(false) }
    var pendingCameraMode by remember { mutableStateOf(false) }
    var currentFrame by remember { mutableStateOf<Bitmap?>(null) }

    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            }

            if (bitmap != null) {
                viewModel.infer(bitmap)
            }
        }

    }

    LaunchedEffect(uiState.result) {
        if (uiState.result != null) {
            useCameraMode = false
            currentFrame = null
            onNavigateToResult()
        }
    }

    LaunchedEffect(cameraPermissionState.status.isGranted, pendingCameraMode) {
        if (pendingCameraMode && cameraPermissionState.status.isGranted) {
            useCameraMode = true
            pendingCameraMode = false
        }
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "나이 추론",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text(
                    text = "나이와 성별을 추론해보세요!",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "gender_q.tflite, age_q.tflite 모델을 활용한 추론을 진행합니다.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                if (useCameraMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        InferenceAgeCameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            onFrameAvailable = { bitmap ->
                                currentFrame = bitmap
                            }
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    useCameraMode = false
                                    currentFrame = null
                                }
                            ) {
                                Text("닫기")
                            }

                            Button(
                                onClick = {
                                    currentFrame?.let { frame ->
                                        viewModel.infer(frame)
                                    }
                                },
                                enabled = currentFrame != null && !uiState.isLoading,
                            ) {
                                Text("촬영해서 분석")
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .background(color = Color.White)
                            .clickable {
                                showSourceDialog = true
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add Photo",
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(text = "사진을 선택하세요.")
                        }
                    }
                }

            }
            if (showSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showSourceDialog = false },
                    title = { Text("사진 가져오기") },
                    text = { Text("가져올 방식을 선택하세요.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showSourceDialog = false
                                if (cameraPermissionState.status.isGranted) {
                                    useCameraMode = true
                                } else {
                                    pendingCameraMode = true
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            }
                        ) {
                            Text("카메라로 촬영")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showSourceDialog = false
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        ) {
                            Text("갤러리에서 선택")
                        }
                    }
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
