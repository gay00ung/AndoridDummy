package net.ifmain.androiddummy.face_rotation.viewmodel

import android.graphics.Rect
import net.ifmain.androiddummy.face_rotation.FaceVerificationAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FaceVerificationViewModelTest {

    @Test
    fun `initial state reflects first provided step`() {
        val viewModel = FaceVerificationViewModel(
            stepProvider = {
                listOf(
                    FaceVerificationViewModel.VerificationStep.TURN_LEFT,
                    FaceVerificationViewModel.VerificationStep.BLINK,
                )
            }
        )

        val state = viewModel.uiState.value

        assertEquals(FaceVerificationViewModel.VerificationStep.TURN_LEFT, state.currentStep)
        assertEquals("천천히 왼쪽으로 돌려주세요", state.instruction)
        assertEquals(0f, state.progress, 0.0001f)
    }

    @Test
    fun `holding a valid face long enough advances to blink step`() {
        var now = 100L
        val viewModel = FaceVerificationViewModel(
            stepProvider = {
                listOf(
                    FaceVerificationViewModel.VerificationStep.LOOK_STRAIGHT,
                    FaceVerificationViewModel.VerificationStep.BLINK,
                )
            },
            blinkTargetProvider = { 2 },
            nowMillis = { now },
        )

        viewModel.processFaceData(faceData())
        now = 1_100L
        viewModel.processFaceData(faceData())

        val state = viewModel.uiState.value
        assertEquals(FaceVerificationViewModel.VerificationStep.BLINK, state.currentStep)
        assertEquals("눈을 2번 깜빡이세요", state.instruction)
        assertEquals(0f, state.progress, 0.0001f)
    }

    @Test
    fun `blink close open cycles complete verification`() {
        var now = 100L
        val viewModel = FaceVerificationViewModel(
            stepProvider = {
                listOf(
                    FaceVerificationViewModel.VerificationStep.LOOK_STRAIGHT,
                    FaceVerificationViewModel.VerificationStep.BLINK,
                )
            },
            blinkTargetProvider = { 2 },
            nowMillis = { now },
        )

        viewModel.processFaceData(faceData())
        now = 1_100L
        viewModel.processFaceData(faceData())

        viewModel.processFaceData(faceData(leftEyeOpenProbability = 0.1f, rightEyeOpenProbability = 0.1f))
        viewModel.processFaceData(faceData(leftEyeOpenProbability = 0.9f, rightEyeOpenProbability = 0.9f))

        assertEquals(0.5f, viewModel.uiState.value.progress, 0.0001f)
        assertTrue(viewModel.uiState.value.instruction.contains("(1/2)"))

        viewModel.processFaceData(faceData(leftEyeOpenProbability = 0.1f, rightEyeOpenProbability = 0.1f))
        viewModel.processFaceData(faceData(leftEyeOpenProbability = 0.9f, rightEyeOpenProbability = 0.9f))

        val state = viewModel.uiState.value
        assertTrue(state.isCompleted)
        assertEquals("인증이 완료되었습니다!", state.instruction)
        assertEquals(1f, state.progress, 0.0001f)
    }

    @Test
    fun `invalid face input resets progress for current step`() {
        var now = 100L
        val viewModel = FaceVerificationViewModel(
            stepProvider = { listOf(FaceVerificationViewModel.VerificationStep.LOOK_STRAIGHT) },
            nowMillis = { now },
        )

        viewModel.processFaceData(faceData())
        now = 500L
        viewModel.processFaceData(faceData(rotY = 40f))

        assertEquals(0f, viewModel.uiState.value.progress, 0.0001f)
        assertEquals(FaceVerificationViewModel.VerificationStep.LOOK_STRAIGHT, viewModel.uiState.value.currentStep)
    }

    private fun faceData(
        rotY: Float = 0f,
        rotX: Float = 0f,
        rotZ: Float = 0f,
        leftEyeOpenProbability: Float? = 0.9f,
        rightEyeOpenProbability: Float? = 0.9f,
        smilingProbability: Float? = 0.8f,
    ): FaceVerificationAnalyzer.FaceData {
        return FaceVerificationAnalyzer.FaceData(
            bounds = Rect(0, 0, 100, 100),
            rotY = rotY,
            rotZ = rotZ,
            rotX = rotX,
            leftEyeOpenProbability = leftEyeOpenProbability,
            rightEyeOpenProbability = rightEyeOpenProbability,
            smilingProbability = smilingProbability,
            trackingId = 1,
        )
    }
}
