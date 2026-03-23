package net.ifmain.androiddummy.age

import android.content.*
import android.graphics.*
import com.google.mlkit.vision.face.*
import org.tensorflow.lite.*
import org.tensorflow.lite.support.common.*
import org.tensorflow.lite.support.common.ops.*
import org.tensorflow.lite.support.image.*
import org.tensorflow.lite.support.image.ops.*
import java.io.*
import kotlin.math.*

class AgeGenderClassifier(
    context: Context
) : Closeable {

    private val appContext = context.applicationContext

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    private val ageInterpreter = Interpreter(
        FileUtil.loadMappedFile(appContext, "model_lite_age_q.tflite")
    )

    private val genderInterpreter = Interpreter(
        FileUtil.loadMappedFile(appContext, "model_gender_q.tflite")
    )

    private val ageImageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(200, 200, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()

    private val genderImageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(128, 128, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()

    fun predictAge(faceBitmap: Bitmap): Int {
        val input = TensorImage.fromBitmap(faceBitmap)
        val processed = ageImageProcessor.process(input)
        val output = Array(1) { FloatArray(1) }

        ageInterpreter.run(processed.buffer, output)

        val age = output[0][0] * 116f
        return floor(age).toInt()
    }

    fun predictGender(faceBitmap: Bitmap): String {
        val input = TensorImage.fromBitmap(faceBitmap)
        val processed = genderImageProcessor.process(input)
        val output = Array(1) { FloatArray(2) }

        genderInterpreter.run(processed.buffer, output)

        return if (output[0][0] > output[0][1]) "남성" else "여성"
    }

    override fun close() {
        faceDetector.close()
        ageInterpreter.close()
        genderInterpreter.close()
    }
}
