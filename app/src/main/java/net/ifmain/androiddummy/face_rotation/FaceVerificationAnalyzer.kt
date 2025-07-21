package net.ifmain.androiddummy.face_rotation

import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

/**
 *
 * @author gayoung.
 * @since 2025. 7. 21.
 */
class FaceVerificationAnalyzer(
    private val onFaceDetected: (FaceData) -> Unit
) : ImageAnalysis.Analyzer {

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
    )

    data class FaceData(
        val bounds: Rect,
        val rotY: Float,  // 좌우 회전 (Yaw)
        val rotZ: Float,  // 기울기 (Roll)
        val rotX: Float,  // 상하 회전 (Pitch)
        val leftEyeOpenProbability: Float?,
        val rightEyeOpenProbability: Float?,
        val smilingProbability: Float?,
        val trackingId: Int?
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        faceDetector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces[0]
                    val faceData = FaceData(
                        bounds = face.boundingBox,
                        rotY = face.headEulerAngleY,
                        rotZ = face.headEulerAngleZ,
                        rotX = face.headEulerAngleX,
                        leftEyeOpenProbability = face.leftEyeOpenProbability,
                        rightEyeOpenProbability = face.rightEyeOpenProbability,
                        smilingProbability = face.smilingProbability,
                        trackingId = face.trackingId
                    )
                    onFaceDetected(faceData)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}