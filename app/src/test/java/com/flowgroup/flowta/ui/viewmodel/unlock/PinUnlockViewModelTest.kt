package com.flowgroup.flowta.ui.viewmodel.unlock

import app.cash.turbine.test
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.pin.VerifyPinUseCase
import com.flowgroup.flowta.ui.state.unlock.PinUnlockEvent
import com.flowgroup.flowta.ui.state.unlock.PinUnlockUiEvent
import com.flowgroup.flowta.ui.state.unlock.PinUnlockUiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PinUnlockViewModelTest {

    private val verifyPin: VerifyPinUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun givenCorrectPin_whenFourDigitsEntered_thenUnlockedEventIsEmitted() = runTest {
        coEvery { verifyPin(any()) } returns Result.Success(true)
        val viewModel = PinUnlockViewModel(verifyPin)

        viewModel.events.test {
            repeat(4) { viewModel.onEvent(PinUnlockEvent.DigitPressed(1)) }
            assertEquals(PinUnlockUiEvent.Unlocked, awaitItem())
        }
    }

    @Test
    fun givenIncorrectPin_whenFourDigitsEntered_thenStateShowsIncorrectErrorAndPinIsCleared() = runTest {
        coEvery { verifyPin(any()) } returns Result.Success(false)
        val viewModel = PinUnlockViewModel(verifyPin)

        repeat(4) { viewModel.onEvent(PinUnlockEvent.DigitPressed(2)) }

        val state = viewModel.uiState.value as PinUnlockUiState.Content
        assertEquals(0, state.enteredLength)
        assertEquals(PinUnlockUiState.Content.Error.Incorrect, state.error)
        assertTrue(!state.isVerifying)
    }

    @Test
    fun givenVerifyFails_whenFourDigitsEntered_thenStateShowsUnexpectedError() = runTest {
        coEvery { verifyPin(any()) } returns Result.Error(AppException.LocalException("disk error"))
        val viewModel = PinUnlockViewModel(verifyPin)

        repeat(4) { viewModel.onEvent(PinUnlockEvent.DigitPressed(3)) }

        val state = viewModel.uiState.value as PinUnlockUiState.Content
        assertEquals(0, state.enteredLength)
        assertEquals(PinUnlockUiState.Content.Error.Unexpected, state.error)
    }

    @Test
    fun givenSomeDigitsEntered_whenBackspacePressed_thenLengthDecreasesAndErrorClears() = runTest {
        val viewModel = PinUnlockViewModel(verifyPin)

        viewModel.onEvent(PinUnlockEvent.DigitPressed(1))
        viewModel.onEvent(PinUnlockEvent.DigitPressed(2))
        viewModel.onEvent(PinUnlockEvent.Backspace)

        val state = viewModel.uiState.value as PinUnlockUiState.Content
        assertEquals(1, state.enteredLength)
        assertEquals(null, state.error)
    }
}
