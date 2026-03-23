package net.ifmain.androiddummy.age

import android.graphics.*
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.*
import androidx.camera.lifecycle.*
import androidx.camera.view.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.*
import androidx.core.content.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.*

/**
 * 나이 추론용 카메라 프리뷰를 표시하고,
 * 최신 프레임을 [Bitmap]으로 변환해 콜백으로 전달한다.
 *
 * 현재는 전면 카메라 프리뷰를 사용하며,
 * 촬영 버튼을 눌렀을 때 사용할 수 있도록 최신 프레임을 유지하는 용도다.
 *
 * @param modifier 프리뷰 레이아웃 수정자
 * @param onFrameAvailable 최신 프레임 비트맵을 전달하는 콜백
 */
@Composable
fun InferenceAgeCameraPreview(
    modifier: Modifier = Modifier,
    onFrameAvailable: (Bitmap) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(cameraExecutor) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = modifier,
        update = { previewView ->
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
                            imageProxy.use { imageProxy ->
                                val bitmap =
                                    imageProxy.toRotatedBitmap(CameraSelector.LENS_FACING_FRONT)
                                ContextCompat.getMainExecutor(context).execute {
                                    onFrameAvailable(bitmap)
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
                        imageAnalyzer
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

/**
 * [ImageProxy]를 회전 및 좌우 반전이 반영된 [Bitmap]으로 변환한다.
 *
 * 전면 카메라 프레임은 화면에서 보이는 방향과 맞추기 위해
 * 회전 보정 후 좌우 반전을 적용한다.
 *
 * @param lensFacing 현재 사용 중인 카메라 렌즈 방향
 * @return 화면 표시 방향에 맞게 보정된 비트맵
 */
@OptIn(ExperimentalGetImage::class)
private fun ImageProxy.toRotatedBitmap(lensFacing: Int): Bitmap {
    val bitmap = toBitmap()
    val matrix = Matrix()
    val rotationDegrees = imageInfo.rotationDegrees

    if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
        when (rotationDegrees) {
            0 -> matrix.postRotate(180f)
            90 -> matrix.postRotate(90f)
            180 -> matrix.postRotate(0f)
            270 -> matrix.postRotate(-90f)
        }
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
    } else {
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
