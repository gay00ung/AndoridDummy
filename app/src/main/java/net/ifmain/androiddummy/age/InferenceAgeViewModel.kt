package net.ifmain.androiddummy.age

import android.app.*
import android.graphics.*
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * 나이/성별 추론 화면에서 사용하는 UI 상태.
 *
 * 추론 진행 여부, 마지막 추론 결과, 오류 메시지를 함께 보관한다.
 * 화면은 이 상태를 구독하여 로딩 표시, 결과 표시, 오류 안내를 처리한다.
 *
 * @property result 마지막으로 계산된 나이/성별 추론 결과
 * @property isLoading 현재 추론 작업이 진행 중인지 여부
 * @property errorMessage 추론 실패 시 화면에 표시할 오류 메시지
 */
data class InferenceAgeUiState(
    val result: AgeGenderResult? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * 사진 기반 나이/성별 추론 화면의 상태와 추론 실행을 관리하는 ViewModel.
 *
 * 내부적으로 [AgeGenderClassifier]를 한 번 생성해 재사용하며,
 * 화면에서는 [uiState]만 관찰하면 로딩 상태와 결과를 반영할 수 있다.
 *
 * [infer]가 호출되면 백그라운드 스레드에서 추론을 수행하고,
 * 성공 시 결과를 상태에 반영하며 실패 시 오류 메시지를 저장한다.
 *
 * @param application 분류기 초기화에 필요한 애플리케이션 인스턴스
 */
class InferenceAgeViewModel(
    application: Application
) : AndroidViewModel(application) {

    /** 실제 얼굴 검출 및 나이/성별 추론을 수행하는 분류기 */
    private val classifier = AgeGenderClassifier(application)

    private val _uiState = MutableStateFlow(InferenceAgeUiState())

    /** 화면이 구독하는 읽기 전용 UI 상태 스트림 */
    val uiState = _uiState.asStateFlow()

    /**
     * 입력된 비트맵에 대해 나이/성별 추론을 시작한다.
     *
     * 추론이 시작되면 로딩 상태를 활성화하고 이전 오류 메시지를 초기화한다.
     * 실제 추론은 백그라운드 스레드에서 수행되며,
     * 성공 시 결과를 상태에 반영하고 실패 시 오류 메시지를 저장한다.
     *
     * @param bitmap 얼굴 검출과 추론에 사용할 원본 이미지
     */
    fun infer(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            runCatching {
                withContext(Dispatchers.Default) {
                    classifier.classify(bitmap)
                }
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        result = result,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "추론 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    /**
     * 현재 보관 중인 추론 결과와 오류 메시지를 초기화한다.
     *
     * 사용자가 다시 이미지를 선택하거나 결과 화면을 정리할 때 호출한다.
     */
    fun clearResult() {
        _uiState.update {
            it.copy(
                result = null,
                errorMessage = null,
            )
        }
    }

    /**
     * ViewModel이 제거될 때 내부 분류기의 리소스를 함께 해제한다.
     */
    override fun onCleared() {
        classifier.close()
        super.onCleared()
    }
}
