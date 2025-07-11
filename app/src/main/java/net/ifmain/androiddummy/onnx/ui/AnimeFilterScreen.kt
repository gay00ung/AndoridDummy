package net.ifmain.androiddummy.onnx.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.provider.MediaStore
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ifmain.androiddummy.onnx.model.AnimeGANModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AndroidDummy
 * Class : AnimeFilterScreen.
 * Created by gayoung.
 * Created On 2025-07-11.
 * Description:
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeFilterScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var animeGANModel by remember { mutableStateOf<AnimeGANModel?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var currentFrame by remember { mutableStateOf<Bitmap?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val model = AnimeGANModel(context)
                model.initialize()
                animeGANModel = model
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            animeGANModel?.close()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("애니메이션 필터") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("뒤로")
                    }
                },
                actions = {
                    // 카메라 전환 버튼
                    if (!showResult) {
                        IconButton(
                            onClick = {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                                    CameraSelector.LENS_FACING_BACK
                                } else {
                                    CameraSelector.LENS_FACING_FRONT
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "카메라 전환"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                )
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 카메라 프리뷰 또는 결과 이미지
            if (!showResult) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onFrameAvailable = { bitmap ->
                        currentFrame = bitmap
                    },
                    cameraExecutor = cameraExecutor,
                    lensFacing = lensFacing
                )
            } else {
                processedBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Processed Image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // 하단 버튼들
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                // 로딩 표시
                if (isProcessing) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("변환 중...")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 버튼들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!showResult) {
                        // 캡처 버튼
                        Button(
                            onClick = {
                                currentFrame?.let { frame ->
                                    capturedBitmap = frame
                                    isProcessing = true
                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            val result = animeGANModel?.processImage(frame)
                                            withContext(Dispatchers.Main) {
                                                processedBitmap = result
                                                showResult = true
                                                isProcessing = false
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            withContext(Dispatchers.Main) {
                                                isProcessing = false
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing && currentFrame != null && animeGANModel != null
                        ) {
                            Text("📸 애니메이션 변환!")
                        }
                    } else {
                        // 다시 찍기 버튼
                        Button(
                            onClick = {
                                showResult = false
                                processedBitmap = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🔄 다시 찍기")
                        }

                        // 저장 버튼
                        Button(
                            onClick = {
                                processedBitmap?.let { bitmap ->
                                    isSaving = true
                                    coroutineScope.launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                saveImageToGallery(context, bitmap)
                                            }
                                            snackbarHostState.showSnackbar(
                                                message = "이미지가 갤러리에 저장되었습니다!",
                                                duration = SnackbarDuration.Short
                                            )
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar(
                                                message = "저장 실패: ${e.message}",
                                                duration = SnackbarDuration.Short
                                            )
                                        } finally {
                                            isSaving = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isSaving && processedBitmap != null
                        ) {
                            Text("💾 저장")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onFrameAvailable: (Bitmap) -> Unit,
    cameraExecutor: ExecutorService,
    lensFacing: Int
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = modifier,
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setResolutionSelector(
                        ResolutionSelector.Builder()
                            .setResolutionStrategy(
                                ResolutionStrategy(
                                    Size(640, 480),
                                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                                )
                            )
                            .build()
                    )
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val bitmap = imageProxy.toRotatedBitmap(lensFacing)
                            onFrameAvailable(bitmap)
                            imageProxy.close()
                        }
                    }

                try {
                    cameraProvider.unbindAll()

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

fun saveImageToGallery(context: Context, bitmap: Bitmap) {
    val filename = "AnimeFilter_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)

        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AnimeFilter")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    uri?.let { imageUri ->
        resolver.openOutputStream(imageUri)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
        }

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(imageUri, values, null, null)
    } ?: throw IOException("Failed to create media file")
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
fun ImageProxy.toRotatedBitmap(lensFacing: Int): Bitmap {
    val bitmap = this.toBitmap()

    val matrix = Matrix()
    val rotationDegrees = imageInfo.rotationDegrees

    if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
        // 전면 카메라 처리
        when (rotationDegrees) {
            0 -> matrix.postRotate(180f)
            90 -> matrix.postRotate(90f)
            180 -> matrix.postRotate(0f)
            270 -> matrix.postRotate(-90f)
        }
        // 좌우 반전
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
    } else {
        // 후면 카메라 처리
        when (rotationDegrees) {
            0 -> matrix.postRotate(0f)
            90 -> matrix.postRotate(90f)
            180 -> matrix.postRotate(180f)
            270 -> matrix.postRotate(270f)
        }
    }

    return if (rotationDegrees != 0 || lensFacing == CameraSelector.LENS_FACING_FRONT) {
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    } else {
        bitmap
    }
}