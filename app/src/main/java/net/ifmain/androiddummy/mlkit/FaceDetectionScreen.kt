package net.ifmain.androiddummy.mlkit

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.ExperimentalGetImage

/**
 * AndroidDummy
 * Class : FingerprintAuthScreen.
 * Created by gayoung.
 * Created On 2025-07-10.
 * Description:
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceDetectionScreen(
    onBack: () -> Unit
) {
    
    var detectedFaces by remember { mutableStateOf<List<Face>>(emptyList()) }
    var emotionState by remember { mutableStateOf("표정을 분석중...") }
    var imageSourceInfo by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("얼굴 표정 인식") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("뒤로")
                    }
                },
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
                CameraPreview(
                    onFaceDetected = { faces, width, height ->
                        detectedFaces = faces
                        emotionState = analyzeEmotion(faces.firstOrNull())
                        imageSourceInfo = Pair(width, height)
                    },
                    faces = detectedFaces
                )

                // Face overlay
                imageSourceInfo?.let { (width, height) ->
                    FaceOverlay(
                        faces = detectedFaces,
                        imageWidth = width,
                        imageHeight = height
                    )
                }
                
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = emotionState,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        if (detectedFaces.isNotEmpty()) {
                            val face = detectedFaces.first()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "웃음 확률: ${String.format("%.1f", (face.smilingProbability ?: 0f) * 100)}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "왼쪽 눈 감김: ${String.format("%.1f", (face.leftEyeOpenProbability ?: 0f) * 100)}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "오른쪽 눈 감김: ${String.format("%.1f", (face.rightEyeOpenProbability ?: 0f) * 100)}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    onFaceDetected: (List<Face>, Int, Int) -> Unit,
    faces: List<Face>
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    val executor = ContextCompat.getMainExecutor(context)
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    val faceDetectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()
    
    val faceDetector = remember { FaceDetection.getClient(faceDetectorOptions) }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            faceDetector.close()
        }
    }
    
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { previewView ->
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                
                                faceDetector.process(image)
                                    .addOnSuccessListener { faces ->
                                        onFaceDetected(faces, imageProxy.width, imageProxy.height)
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            }
                        }
                    }
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)
        }
    )
}

fun analyzeEmotion(face: Face?): String {
    if (face == null) return "얼굴을 찾을 수 없습니다"
    
    val smilingProbability = face.smilingProbability ?: -1f
    val leftEyeOpenProbability = face.leftEyeOpenProbability ?: -1f
    val rightEyeOpenProbability = face.rightEyeOpenProbability ?: -1f
    
    return when {
        smilingProbability > 0.8f -> "😊 행복해 보입니다!"
        smilingProbability > 0.5f -> "🙂 미소를 짓고 있습니다"
        leftEyeOpenProbability < 0.3f && rightEyeOpenProbability < 0.3f -> "😴 졸려 보입니다"
        leftEyeOpenProbability < 0.5f || rightEyeOpenProbability < 0.5f -> "😉 윙크하고 있습니다"
        smilingProbability < 0.2f -> "😐 무표정입니다"
        else -> "🤔 표정을 분석중..."
    }
}