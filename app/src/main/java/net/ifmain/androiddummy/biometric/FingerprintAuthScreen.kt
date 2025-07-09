package net.ifmain.androiddummy.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

/**
 * AndroidDummy
 * Class : FingerprintAuthScreen.
 * Created by gayoung.
 * Created On 2025-07-09.
 * Description:
 */
@Composable
fun FingerprintAuthScreen() {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    var authStatus by remember { mutableStateOf("대기 중") }
    var isAuthenticating by remember { mutableStateOf(false) }

    // BiometricPrompt 설정
    val executor: Executor = ContextCompat.getMainExecutor(context)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                authStatus = "인증 오류: $errString"
                isAuthenticating = false
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                authStatus = "인증 성공!"
                isAuthenticating = false
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                authStatus = "인증 실패"
                isAuthenticating = false
            }
        })

    // 생체 인식 프롬프트 정보 설정
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("지문 인증")
        .setSubtitle("등록된 지문으로 인증해주세요")
        .setDescription("지문을 센서에 올려주세요")
        .setNegativeButtonText("취소")
        .build()

    // 지문 인식 기능
    fun authenticateWithFingerprint() {
        when (BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                authStatus = "지문을 인식 중..."
                isAuthenticating = true
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                authStatus = "생체 인식 하드웨어가 없습니다"
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                authStatus = "생체 인식 하드웨어를 사용할 수 없습니다"
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                authStatus = "등록된 생체 인식 정보가 없습니다"
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                authStatus = "보안 업데이트가 필요합니다"
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                authStatus = "생체 인식이 지원되지 않습니다"
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                authStatus = "생체 인식 상태를 알 수 없습니다"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "지문 인증 앱",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "인증 상태",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = authStatus,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = when {
                        authStatus.contains("성공") -> MaterialTheme.colorScheme.primary
                        authStatus.contains("실패") || authStatus.contains("오류") -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { authenticateWithFingerprint() },
                    enabled = !isAuthenticating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("인증 중...")
                    } else {
                        Text(
                            text = "지문 인식하기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "버튼을 눌러 지문 인증을 시작하세요",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}