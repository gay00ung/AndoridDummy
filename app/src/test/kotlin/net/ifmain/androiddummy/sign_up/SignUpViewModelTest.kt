package net.ifmain.androiddummy.sign_up

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Test

class SignUpViewModelTest {

    @Test
    fun `initial state is restored from saved state handle`() {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "sign_up_name" to "가영",
                "sign_up_user_id" to "gayoung123",
                "sign_up_password" to "secret",
            )
        )

        val viewModel = SignUpViewModel(savedStateHandle)

        assertEquals(
            SignUpState(
                name = "가영",
                userId = "gayoung123",
                password = "secret",
            ),
            viewModel.state.value,
        )
    }

    @Test
    fun `setters update both ui state and saved state handle`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = SignUpViewModel(savedStateHandle)

        viewModel.setName("Kim")
        viewModel.setUserId("kim-user")
        viewModel.setPassword("pw-1234")

        assertEquals(
            SignUpState(
                name = "Kim",
                userId = "kim-user",
                password = "pw-1234",
            ),
            viewModel.state.value,
        )
        assertEquals("Kim", savedStateHandle["sign_up_name"])
        assertEquals("kim-user", savedStateHandle["sign_up_user_id"])
        assertEquals("pw-1234", savedStateHandle["sign_up_password"])
    }

    @Test
    fun `clear resets state and persisted values`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = SignUpViewModel(savedStateHandle)

        viewModel.setName("Kim")
        viewModel.setUserId("kim-user")
        viewModel.setPassword("pw-1234")
        viewModel.clear()

        assertEquals(SignUpState(), viewModel.state.value)
        assertEquals("", savedStateHandle["sign_up_name"])
        assertEquals("", savedStateHandle["sign_up_user_id"])
        assertEquals("", savedStateHandle["sign_up_password"])
    }
}
