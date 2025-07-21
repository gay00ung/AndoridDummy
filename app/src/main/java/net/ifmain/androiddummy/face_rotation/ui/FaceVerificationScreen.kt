package net.ifmain.androiddummy.face_rotation.ui

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import net.ifmain.androiddummy.face_rotation.viewmodel.FaceVerificationViewModel

/**
 *
 * @author gayoung.
 * @since 2025. 7. 21.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceVerificationScreen(
    onVerificationComplete: () -> Unit
) {
    val viewModel: FaceVerificationViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    LaunchedEffect(cameraPermissionState.status) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .systemBarsPadding()
        ) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onFaceDetected = { faceData ->
                    viewModel.processFaceData(faceData)
                }
            )

            FaceGuideOverlay(
                modifier = Modifier.fillMaxSize(),
                faceData = uiState.faceData,
                currentStep = uiState.currentStep
            )

            VerificationUI(
                modifier = Modifier.align(Alignment.BottomCenter),
                instruction = uiState.instruction,
                progress = uiState.progress
            )
        }

        if (uiState.isCompleted) {
            LaunchedEffect(Unit) {
                delay(2000)
                onVerificationComplete()
            }
        }
    }
}