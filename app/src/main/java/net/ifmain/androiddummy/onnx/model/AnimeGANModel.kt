package net.ifmain.androiddummy.onnx.model

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.scale
import java.nio.FloatBuffer
import androidx.core.graphics.createBitmap

/**
 * AndroidDummy
 * Class : AnimeGANModel.
 * Created by gayoung.
 * Created On 2025-07-11.
 * Description:
 */
class AnimeGANModel(private val context: Context) {
    private lateinit var ortSession: OrtSession
    private lateinit var ortEnvironment: OrtEnvironment

    fun initialize() {
        println("🔥 AnimeGANModel 초기화 시작")
        ortEnvironment = OrtEnvironment.getEnvironment()

        val modelBytes = context.assets.open("model_float32_opt.onnx").use {
            it.readBytes()
        }

        val sessionOptions = OrtSession.SessionOptions().apply {
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            setExecutionMode(OrtSession.SessionOptions.ExecutionMode.SEQUENTIAL)
        }

        ortSession = ortEnvironment.createSession(modelBytes, sessionOptions)

        // 모델 입력/출력 정보 출력
        println("🔥 === 모델 정보 ===")
        println("🔥 입력 이름들: ${ortSession.inputNames}")
        println("🔥 출력 이름들: ${ortSession.outputNames}")

        println("🔥 AnimeGANModel 초기화 완료!")
    }

    // 비트맵을 AnimeGAN 스타일로 변환
    fun processImage(bitmap: Bitmap): Bitmap {
        val squareBitmap = centerCropSquare(bitmap)
        val resizedBitmap = squareBitmap.scale(256, 256)
        val inputArray = bitmapToFloatArray(resizedBitmap)
        val outputArray = runModel(inputArray)

        return floatArrayToBitmap(outputArray)
    }

    private fun centerCropSquare(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = minOf(width, height)

        val x = (width - size) / 2
        val y = (height - size) / 2

        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    private fun bitmapToFloatArray(bitmap: Bitmap): FloatArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // NHWC 형식 (모델이 요구하는 형식)
        val floatArray = FloatArray(width * height * 3)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = pixels[y * width + x]

                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                // NHWC 형식으로 저장
                val index = (y * width + x) * 3
                floatArray[index] = (r.toFloat() / 127.5f) - 1f      // R
                floatArray[index + 1] = (g.toFloat() / 127.5f) - 1f  // G
                floatArray[index + 2] = (b.toFloat() / 127.5f) - 1f  // B
            }
        }

        return floatArray
    }

    private fun floatArrayToBitmap(floatArray: FloatArray): Bitmap {
        val width = 256
        val height = 256
        val bitmap = createBitmap(width, height)
        val pixels = IntArray(width * height)

        // NHWC 형식에서 픽셀로 변환
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = (y * width + x) * 3

                // -1~1을 0~255로 변환
                val r = ((floatArray[index] + 1f) * 127.5f).toInt().coerceIn(0, 255)
                val g = ((floatArray[index + 1] + 1f) * 127.5f).toInt().coerceIn(0, 255)
                val b = ((floatArray[index + 2] + 1f) * 127.5f).toInt().coerceIn(0, 255)

                pixels[y * width + x] = Color.argb(255, r, g, b)
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun runModel(inputArray: FloatArray): FloatArray {
        val inputName = ortSession.inputNames.iterator().next()
        val shape = longArrayOf(1, 256, 256, 3)  // NHWC 형식

        val inputTensor = OnnxTensor.createTensor(
            ortEnvironment,
            FloatBuffer.wrap(inputArray),
            shape
        )

        val inputs = mapOf(inputName to inputTensor)
        val outputs = ortSession.run(inputs)

        val outputName = ortSession.outputNames.iterator().next()
        val outputTensor = outputs[outputName].get() as OnnxTensor
        val outputBuffer = outputTensor.floatBuffer

        val outputArray = FloatArray(outputBuffer.remaining())
        outputBuffer.get(outputArray)

        inputTensor.close()
        outputs.close()

        return outputArray
    }

    fun close() {
        if (::ortSession.isInitialized) {
            ortSession.close()
        }
        if (::ortEnvironment.isInitialized) {
            ortEnvironment.close()
        }
    }
}