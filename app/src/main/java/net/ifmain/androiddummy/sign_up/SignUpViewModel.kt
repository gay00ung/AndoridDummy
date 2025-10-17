package net.ifmain.androiddummy.sign_up

import androidx.lifecycle.*
import kotlinx.coroutines.flow.*

data class SignUpState(
    val name: String = "",
    val userId: String = "",
    val password: String = "",
)

class SignUpViewModel(
    private val saved: SavedStateHandle
) : ViewModel() {
    private val KEY_NAME = "sign_up_name"
    private val KEY_USER_ID = "sign_up_user_id"
    private val KEY_PASSWORD = "sign_up_password"

    private val _state = MutableStateFlow(
        SignUpState(
            name = saved[KEY_NAME] ?: "",
            userId = saved[KEY_USER_ID] ?: "",
            password = saved[KEY_PASSWORD] ?: "",
        )
    )
    val state = _state.asStateFlow()

    private fun persist() {
        val value = _state.value
        saved[KEY_NAME] = value.name
        saved[KEY_USER_ID] = value.userId
        saved[KEY_PASSWORD] = value.password
    }

    fun setName(name: String) {
        _state.update { it.copy(name = name) }
        persist()
    }

    fun setUserId(userId: String) {
        _state.update { it.copy(userId = userId) }
        persist()
    }

    fun setPassword(password: String) {
        _state.update { it.copy(password = password) }
        persist()
    }

    fun clear() {
        _state.value = SignUpState()
        persist()
    }
}
