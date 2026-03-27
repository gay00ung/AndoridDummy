package net.ifmain.androiddummy.age

import android.app.Application
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.ifmain.androiddummy.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class InferenceAgeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)

    @Test
    fun `infer success updates ui state with result`() = runTest(mainDispatcherRule.dispatcher) {
        val expected = AgeGenderResult(
            faceBitmap = bitmap,
            age = 31,
            gender = "여성",
        )
        val engine = FakeAgeGenderEngine(result = Result.success(expected))
        val viewModel = InferenceAgeViewModel(
            application = application,
            classifier = engine,
            workerDispatcher = mainDispatcherRule.dispatcher,
        )

        viewModel.infer(bitmap)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertSame(expected, state.result)
        assertNull(state.errorMessage)
    }

    @Test
    fun `infer failure exposes error message`() = runTest(mainDispatcherRule.dispatcher) {
        val engine = FakeAgeGenderEngine(
            result = Result.failure(IllegalStateException("분류 실패"))
        )
        val viewModel = InferenceAgeViewModel(
            application = application,
            classifier = engine,
            workerDispatcher = mainDispatcherRule.dispatcher,
        )

        viewModel.infer(bitmap)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.result)
        assertEquals("분류 실패", state.errorMessage)
    }

    @Test
    fun `clearResult clears stale data and onCleared closes engine`() = runTest(mainDispatcherRule.dispatcher) {
        val expected = AgeGenderResult(
            faceBitmap = bitmap,
            age = 31,
            gender = "여성",
        )
        val engine = FakeAgeGenderEngine(result = Result.success(expected))
        val viewModel = InferenceAgeViewModel(
            application = application,
            classifier = engine,
            workerDispatcher = mainDispatcherRule.dispatcher,
        )

        viewModel.infer(bitmap)
        advanceUntilIdle()
        viewModel.clearResult()

        assertNull(viewModel.uiState.value.result)
        assertNull(viewModel.uiState.value.errorMessage)

        val onCleared = InferenceAgeViewModel::class.java.getDeclaredMethod("onCleared")
        onCleared.isAccessible = true
        onCleared.invoke(viewModel)

        assertTrue(engine.closed)
    }

    private class FakeAgeGenderEngine(
        private val result: Result<AgeGenderResult>
    ) : AgeGenderEngine {
        var closed = false

        override suspend fun classify(bitmap: Bitmap): AgeGenderResult {
            return result.getOrThrow()
        }

        override fun close() {
            closed = true
        }
    }
}
