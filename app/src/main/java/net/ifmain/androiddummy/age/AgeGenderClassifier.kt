package net.ifmain.androiddummy.age

import android.content.*
import android.graphics.*
import com.google.mlkit.vision.common.*
import com.google.mlkit.vision.face.*
import kotlinx.coroutines.*
import org.tensorflow.lite.*
import org.tensorflow.lite.support.common.*
import org.tensorflow.lite.support.common.ops.*
import org.tensorflow.lite.support.image.*
import org.tensorflow.lite.support.image.ops.*
import java.io.*
import kotlin.coroutines.*
import kotlin.math.*

/**
 * 입력 이미지에서 얼굴을 검출한 뒤, 가장 큰 얼굴 영역을 기준으로
 * 나이와 성별을 예측하는 분류기 클래스.
 *
 * 내부적으로 ML Kit 얼굴 검출기를 사용하여 얼굴 위치를 찾고,
 * 잘라낸 얼굴 이미지를 각각 나이 예측 모델과 성별 예측 모델에 입력한다.
 *
 * 이 클래스는 하나의 이미지에서 가장 크게 검출된 얼굴만을 대상으로 하며,
 * 얼굴이 검출되지 않으면 분류를 수행할 수 없다.
 *
 * 사용이 끝난 후에는 [close]를 호출하여 얼굴 검출기와
 * TensorFlow Lite 인터프리터 리소스를 해제해야 한다.
 *
 * @property appContext 모델 파일 로드에 사용하는 애플리케이션 컨텍스트
 * @constructor 애플리케이션 또는 컴포넌트 컨텍스트를 받아 분류기를 생성한다.
 * @param context 모델 파일과 리소스에 접근하기 위한 컨텍스트
 */
class AgeGenderClassifier(
    context: Context
) : AgeGenderEngine {

    private val appContext = context.applicationContext

    /** 입력 이미지에서 얼굴 위치를 검출하기 위한 ML Kit 얼굴 검출기 */
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    /** 나이 예측 모델을 실행하는 TensorFlow Lite 인터프리터 */
    private val ageInterpreter = Interpreter(
        FileUtil.loadMappedFile(appContext, "model_lite_age_q.tflite")
    )

    /** 성별 예측 모델을 실행하는 TensorFlow Lite 인터프리터 */
    private val genderInterpreter = Interpreter(
        FileUtil.loadMappedFile(appContext, "model_gender_q.tflite")
    )

    /** 나이 예측 모델 입력 형식에 맞게 얼굴 이미지를 전처리하는 프로세서 */
    private val ageImageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(200, 200, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()

    /** 성별 예측 모델 입력 형식에 맞게 얼굴 이미지를 전처리하는 프로세서 */
    private val genderImageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(128, 128, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()

    /**
     * 잘라낸 얼굴 이미지를 입력받아 나이를 예측한다.
     *
     * 입력 이미지는 나이 예측 모델의 입력 형식에 맞게 전처리된 뒤
     * TensorFlow Lite 인터프리터를 통해 추론된다.
     *
     * 모델 출력값은 내부 스케일에 따라 실제 나이 범위로 변환한 후,
     * 소수점 이하는 버리고 정수 나이로 반환한다.
     *
     * @param faceBitmap 얼굴 영역만 포함하는 비트맵
     * @return 예측된 나이
     */
    fun predictAge(faceBitmap: Bitmap): Int {
        val input = TensorImage.fromBitmap(faceBitmap)
        val processed = ageImageProcessor.process(input)
        val output = Array(1) { FloatArray(1) }

        ageInterpreter.run(processed.buffer, output)

        val age = output[0][0] * 116f
        return floor(age).toInt()
    }

    /**
     * 잘라낸 얼굴 이미지를 입력받아 성별을 예측한다.
     *
     * 입력 이미지는 성별 예측 모델의 입력 형식에 맞게 전처리된 뒤
     * TensorFlow Lite 인터프리터를 통해 추론된다.
     * 모델의 두 출력값을 비교하여 더 큰 쪽의 클래스를 반환한다.
     *
     * @param faceBitmap 얼굴 영역만 포함하는 비트맵
     * @return 예측된 성별 문자열
     */
    fun predictGender(faceBitmap: Bitmap): String {
        val input = TensorImage.fromBitmap(faceBitmap)
        val processed = genderImageProcessor.process(input)
        val output = Array(1) { FloatArray(2) }

        genderInterpreter.run(processed.buffer, output)

        return if (output[0][0] > output[0][1]) "남성" else "여성"
    }

    /**
     * 입력 이미지에서 가장 큰 얼굴을 검출한 뒤,
     * 해당 얼굴을 기준으로 나이와 성별을 함께 예측한다.
     *
     * 처리 순서는 다음과 같다.
     * 1. 이미지에서 가장 큰 얼굴 영역을 검출한다.
     * 2. 검출된 얼굴 영역을 잘라낸다.
     * 3. 잘라낸 얼굴 이미지로 나이를 예측한다.
     * 4. 잘라낸 얼굴 이미지로 성별을 예측한다.
     *
     * 얼굴이 검출되지 않으면 [IllegalStateException]을 발생시킨다.
     *
     * @param bitmap 분류 대상 원본 이미지
     * @return 잘라낸 얼굴 이미지와 예측 결과를 포함한 [AgeGenderResult]
     * @throws IllegalStateException 얼굴을 찾을 수 없는 경우
     */
    override suspend fun classify(bitmap: Bitmap): AgeGenderResult {
        val faceRect = detectLargestFace(bitmap)
            ?: throw IllegalStateException("얼굴을 찾을 수 없습니다.")

        val faceBitmap = cropFace(bitmap, faceRect)
        val age = predictAge(faceBitmap)
        val gender = predictGender(faceBitmap)

        return AgeGenderResult(
            faceBitmap = faceBitmap,
            age = age,
            gender = gender,
        )
    }

    /**
     * 입력 이미지에서 가장 크게 검출된 얼굴의 경계 영역을 반환한다.
     *
     * 여러 얼굴이 검출된 경우 바운딩 박스 면적이 가장 큰 얼굴을 선택한다.
     * 얼굴이 검출되지 않으면 null을 반환한다.
     *
     * @param bitmap 얼굴 검출 대상 이미지
     * @return 가장 큰 얼굴의 경계 사각형, 없으면 null
     */
    private suspend fun detectLargestFace(bitmap: Bitmap): Rect? =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)

            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    val largestFace = faces.maxByOrNull {
                        it.boundingBox.width() * it.boundingBox.height()
                    }
                    continuation.resume(largestFace?.boundingBox)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }

    /**
     * 원본 이미지와 얼굴 경계 영역을 받아,
     * 주변에 약간의 여백을 포함한 얼굴 비트맵을 생성한다.
     *
     * 얼굴 경계의 가로와 세로에 각각 10%의 패딩을 추가하며,
     * 잘라내는 영역이 원본 이미지 범위를 벗어나지 않도록 보정한다.
     *
     * @param bitmap 원본 이미지
     * @param rect 얼굴 경계 영역
     * @return 잘라낸 얼굴 비트맵
     */
    private fun cropFace(bitmap: Bitmap, rect: Rect): Bitmap {
        val paddingX = (rect.width() * 0.1f).toInt()
        val paddingY = (rect.height() * 0.1f).toInt()

        val left = (rect.left - paddingX).coerceAtLeast(0)
        val top = (rect.top - paddingY).coerceAtLeast(0)
        val right = (rect.right + paddingX).coerceAtMost(bitmap.width)
        val bottom = (rect.bottom + paddingY).coerceAtMost(bitmap.height)

        return Bitmap.createBitmap(
            bitmap,
            left,
            top,
            right - left,
            bottom - top
        )
    }

    /**
     * 내부에서 사용하는 얼굴 검출기와 TensorFlow Lite 인터프리터를 해제한다.
     *
     * 분류기 사용이 끝난 후 호출해야 하며,
     * 호출하지 않으면 리소스가 해제되지 않을 수 있다.
     */
    override fun close() {
        faceDetector.close()
        ageInterpreter.close()
        genderInterpreter.close()
    }
}
