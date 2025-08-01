package net.ifmain.androiddummy.gemini_nano

import android.content.Context
import android.util.Log
import com.google.ai.edge.aicore.GenerativeAIException
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion

/**
 * Gemini Nano 모델을 관리하는 클래스
 * @author gayoung
 * @since 2025. 8. 1.
 */
class GeminiNanoManager(private val context: Context) {

    companion object {
        private const val TAG = "GeminiNanoManager"
    }

    private var model: GenerativeModel? = null

    suspend fun initializeModel(): Boolean {
        return try {
            Log.d(TAG, "Gemini Nano 모델 초기화 시작...")
            
            // AICore 서비스 가용성 확인
            if (!isAICoreAvailable()) {
                Log.e(TAG, "AICore 서비스를 사용할 수 없습니다. 디바이스가 지원되지 않거나 Google Play Services가 최신 버전이 아닙니다.")
                return false
            }
            
            model = GenerativeModel(
                generationConfig {
                    context = this@GeminiNanoManager.context
                    temperature = 0.7f
                    topK = 40
                    maxOutputTokens = 8192
                }
            )
            Log.d(TAG, "GenerativeModel 인스턴스 생성 완료")
            
            // Prepare the inference engine
            model?.prepareInferenceEngine()
            Log.d(TAG, "Inference engine 준비 완료")
            
            true
        } catch (e: GenerativeAIException) {
            Log.e(TAG, "GenerativeAIException 발생: ${e.message}", e)
            when {
                e.message?.contains("BINDING_FAILURE") == true -> {
                    Log.e(TAG, "AICore 서비스 바인딩 실패. Google Play Services를 업데이트하거나 디바이스 호환성을 확인하세요.")
                }
                e.message?.contains("CONNECTION_ERROR") == true -> {
                    Log.e(TAG, "AICore 연결 실패. 네트워크 연결과 Google 계정 로그인을 확인하세요.")
                }
                else -> {
                    Log.e(TAG, "알 수 없는 AI 오류: ${e.message}")
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "일반 Exception 발생: ${e.message}", e)
            false
        }
    }
    
    private fun isAICoreAvailable(): Boolean {
        return try {
            // Google Play Services 버전 확인
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo("com.google.android.gms", 0)
            Log.d(TAG, "Google Play Services 버전: ${packageInfo.versionName}")
            
            // AICore 패키지 확인
            try {
                packageManager.getPackageInfo("com.google.android.aicore", 0)
                Log.d(TAG, "AICore 패키지가 설치되어 있습니다.")
                true
            } catch (e: Exception) {
                Log.e(TAG, "AICore 패키지가 설치되어 있지 않습니다.")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google Play Services 확인 실패: ${e.message}")
            false
        }
    }
    
    suspend fun generateText(prompt: String): String {
        return try {
            model?.let { generativeModel ->
                val response = generativeModel.generateContent(prompt)
                response.text ?: "응답을 생성할 수 없습니다."
            } ?: throw IllegalStateException("Model not initialized")
        } catch (e: GenerativeAIException) {
            "AI 오류 발생: ${e.message}"
        } catch (e: Exception) {
            "오류 발생: ${e.message}"
        }
    }
    
    fun generateTextStream(prompt: String): Flow<String> {
        return try {
            model?.let { generativeModel ->
                generativeModel.generateContentStream(prompt)
                    .map { response -> response.text ?: "" }
                    .onCompletion { cause ->
                        if (cause != null) {
                            println("Stream completed with error: ${cause.message}")
                        }
                    }
            } ?: flow {
                emit("Model not initialized")
            }
        } catch (e: GenerativeAIException) {
            flow {
                emit("AI 스트림 오류 발생: ${e.message}")
            }
        } catch (e: Exception) {
            flow {
                emit("스트림 오류 발생: ${e.message}")
            }
        }
    }
    
    fun isModelReady(): Boolean {
        return model != null
    }
    
    fun cleanup() {
        model?.close()
        model = null
    }
}