package net.ifmain.androiddummy.gemini_nano

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Gemini Nano UI 상태를 관리하는 ViewModel
 * @author gayoung
 * @since 2025. 8. 1.
 */
class GeminiNanoViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "GeminiNanoViewModel"
    }
    
    private val geminiNanoManager = GeminiNanoManager(application)
    
    private val _uiState = MutableStateFlow(GeminiNanoUiState())
    val uiState: StateFlow<GeminiNanoUiState> = _uiState.asStateFlow()
    
    private val _generatedText = MutableStateFlow("")
    val generatedText: StateFlow<String> = _generatedText.asStateFlow()
    
    init {
        initializeGeminiNano()
    }
    
    private fun initializeGeminiNano() {
        Log.d(TAG, "ViewModel에서 Gemini Nano 초기화 시작")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val success = geminiNanoManager.initializeModel()
            Log.d(TAG, "초기화 결과: $success")
            
            val errorMessage = if (!success) {
                """
                Gemini Nano 초기화 실패
                
                가능한 원인:
                1. AICore 서비스가 설치되지 않았습니다
                2. 디바이스가 Gemini Nano를 지원하지 않습니다
                3. Google Play Services 업데이트가 필요합니다
                
                해결 방법:
                - Google Play Store에서 업데이트 확인
                - 디바이스 재시작 후 다시 시도
                """.trimIndent()
            } else null
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isModelReady = success,
                errorMessage = errorMessage
            )
            
            if (!success) {
                Log.e(TAG, "Gemini Nano 초기화 실패!")
            }
        }
    }
    
    fun generateText(prompt: String) {
        if (!geminiNanoManager.isModelReady()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "모델이 준비되지 않았습니다"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGenerating = true,
                errorMessage = null
            )
            
            val text = geminiNanoManager.generateText(prompt)
            _generatedText.value = text
            _uiState.value = _uiState.value.copy(isGenerating = false)
        }
    }
    
    fun generateTextStream(prompt: String) {
        if (!geminiNanoManager.isModelReady()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "모델이 준비되지 않았습니다"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGenerating = true,
                errorMessage = null
            )
            _generatedText.value = ""
            
            geminiNanoManager.generateTextStream(prompt).collect { chunk ->
                _generatedText.value += chunk
            }
            
            _uiState.value = _uiState.value.copy(isGenerating = false)
        }
    }
    
    fun clearText() {
        _generatedText.value = ""
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        geminiNanoManager.cleanup()
    }
}

data class GeminiNanoUiState(
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val isModelReady: Boolean = false,
    val errorMessage: String? = null
)