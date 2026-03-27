package net.ifmain.androiddummy.face_rotation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.ifmain.androiddummy.face_rotation.FaceVerificationAnalyzer
import kotlin.math.abs
import kotlin.random.Random

/**
 *
 * @author gayoung.
 * @since 2025. 7. 21.
 */
class FaceVerificationViewModel(
    private val stepProvider: () -> List<VerificationStep> = {
        defaultVerificationSteps()
    },
    private val blinkTargetProvider: () -> Int = {
        Random.nextInt(1, 6)
    },
    private val nowMillis: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private var currentStepIndex = 0
    private var stepStartTime: Long? = null
    private val requiredDuration = 1000L

    private var targetBlinkCount = 0
    private var currentBlinkCount = 0
    private var wasEyesClosed = false
    private val verificationSteps = stepProvider()
    private val initialStep = verificationSteps.firstOrNull() ?: VerificationStep.LOOK_STRAIGHT

    private val _uiState = MutableStateFlow(
        FaceVerificationUiState(
            currentStep = initialStep,
            instruction = initialStep.instruction
        )
    )
    val uiState: StateFlow<FaceVerificationUiState> = _uiState.asStateFlow()

    private companion object {
        fun defaultVerificationSteps(): List<VerificationStep> {
        val directionSteps = listOf(
            VerificationStep.LOOK_STRAIGHT,
            VerificationStep.TURN_LEFT,
            VerificationStep.TURN_RIGHT,
            VerificationStep.TURN_UP,
            VerificationStep.TURN_DOWN,
            VerificationStep.SMILE,
            VerificationStep.WINKLEFT,
            VerificationStep.WINKRIGHT
        )

        val randomDirection = directionSteps.random()

        return listOf(randomDirection, VerificationStep.BLINK)
        }
    }

    data class FaceVerificationUiState(
        val currentStep: VerificationStep = VerificationStep.LOOK_STRAIGHT,
        val progress: Float = 0f,
        val isCompleted: Boolean = false,
        val faceData: FaceVerificationAnalyzer.FaceData? = null,
        val instruction: String = "정면을 바라봐주세요",
        val error: String? = null
    )

    enum class VerificationStep(
        val instruction: String,
        val targetAngle: Float = 0f,
        val validation: (FaceVerificationAnalyzer.FaceData) -> Boolean
    ) {
        LOOK_STRAIGHT(
            "정면을 바라봐주세요",
            0f,
            { face -> abs(face.rotY) < 10f && abs(face.rotX) < 10f }
        ),
        TURN_LEFT(
            "천천히 왼쪽으로 돌려주세요",
            30f,
            { face -> face.rotY > 20f }
        ),
        TURN_RIGHT(
            "천천히 오른쪽으로 돌려주세요",
            -30f,
            { face -> face.rotY < -20f }
        ),
        TURN_UP(
            "천천히 위로 올려주세요",
            30f,
            { face -> face.rotX > 20f }
        ),
        TURN_DOWN(
            "천천히 아래로 내려주세요",
            -30f,
            { face -> face.rotX < -20f }
        ),
        SMILE(
            "웃어주세요",
            0f,
            { face -> face.smilingProbability?.let { it > 0.05f } ?: false }
        ),
        BLINK(
            "눈을 깜빡여주세요",
            0f,
            { face ->
                face.leftEyeOpenProbability?.let { it < 0.3f } ?: false &&
                        face.rightEyeOpenProbability?.let { it < 0.3f } ?: false
            }
        ),
        WINKLEFT(
            "왼쪽 눈을 감아주세요",
            0f,
            { face ->
                (face.leftEyeOpenProbability?.let { it < 0.3f } ?: false) !=
                        (face.rightEyeOpenProbability?.let { it > 0.3f } ?: false)
            }
        ),
        WINKRIGHT(
            "오른쪽 눈을 감아주세요",
            0f,
            { face ->
                (face.leftEyeOpenProbability?.let { it > 0.3f } ?: false) !=
                        (face.rightEyeOpenProbability?.let { it < 0.3f } ?: false)
            }
        )
    }

    private fun processBlinkStep(faceData: FaceVerificationAnalyzer.FaceData) {
        val eyesClosed = faceData.leftEyeOpenProbability?.let { it < 0.3f } ?: false &&
                faceData.rightEyeOpenProbability?.let { it < 0.3f } ?: false

        if (wasEyesClosed && !eyesClosed) {
            currentBlinkCount++

            val progress = currentBlinkCount.toFloat() / targetBlinkCount
            _uiState.update {
                it.copy(
                    progress = progress,
                    instruction = "눈을 ${targetBlinkCount}번 깜빡이세요 (${currentBlinkCount}/${targetBlinkCount})"
                )
            }

            if (currentBlinkCount >= targetBlinkCount) {
                moveToNextStep()
            }
        }

        wasEyesClosed = eyesClosed
    }

    fun processFaceData(faceData: FaceVerificationAnalyzer.FaceData) {
        val currentStep = verificationSteps.getOrNull(currentStepIndex) ?: return

        println("Face angles - rotY: ${faceData.rotY}, rotX: ${faceData.rotX}, rotZ: ${faceData.rotZ}")
        println("Current step: ${currentStep.name}, Target angle: ${currentStep.targetAngle}")
        println("Smile probability: ${faceData.smilingProbability}, Eyes open: L=${faceData.leftEyeOpenProbability}, R=${faceData.rightEyeOpenProbability}")

        _uiState.update { it.copy(faceData = faceData) }

        if (currentStep == VerificationStep.BLINK) {
            processBlinkStep(faceData)
        } else {
            if (currentStep.validation(faceData)) {
                if (stepStartTime == null) {
                    stepStartTime = nowMillis()
                }

                val elapsedTime = nowMillis() - (stepStartTime ?: nowMillis())
                val progress = (elapsedTime.toFloat() / requiredDuration).coerceIn(0f, 1f)

                _uiState.update {
                    it.copy(
                        progress = progress,
                        currentStep = currentStep
                    )
                }

                if (elapsedTime >= requiredDuration) {
                    moveToNextStep()
                }
            } else {
                stepStartTime = null
                _uiState.update {
                    it.copy(
                        progress = 0f,
                        currentStep = currentStep
                    )
                }
            }
        }
    }

    private fun moveToNextStep() {
        currentStepIndex++
        stepStartTime = null

        currentBlinkCount = 0
        wasEyesClosed = false

        if (currentStepIndex < verificationSteps.size) {
            val nextStep = verificationSteps[currentStepIndex]

            if (nextStep == VerificationStep.BLINK) {
                targetBlinkCount = blinkTargetProvider()
                _uiState.update {
                    it.copy(
                        currentStep = nextStep,
                        instruction = "눈을 ${targetBlinkCount}번 깜빡이세요",
                        progress = 0f
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        currentStep = nextStep,
                        instruction = nextStep.instruction,
                        progress = 0f
                    )
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    isCompleted = true,
                    instruction = "인증이 완료되었습니다!"
                )
            }
        }
    }
}
