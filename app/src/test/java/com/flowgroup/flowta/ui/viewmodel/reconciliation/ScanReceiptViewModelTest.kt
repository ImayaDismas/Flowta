package com.flowgroup.flowta.ui.viewmodel.reconciliation

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.ParseOutcome
import com.flowgroup.flowta.domain.model.PaymentSource
import com.flowgroup.flowta.domain.reconciliation.ReceiptTextRecognizer
import com.flowgroup.flowta.domain.usecase.reconciliation.ParseAndStorePaymentsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanReceiptViewModelTest {

    private val recognizer: ReceiptTextRecognizer = mockk()
    private val parseAndStore: ParseAndStorePaymentsUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ScanReceiptViewModel(recognizer, parseAndStore)

    @Test
    fun givenReadableImage_whenImagePicked_thenRecognizedTextIsParsedAsCameraOcr() = runTest {
        coEvery { recognizer.recognize("content://img") } returns Result.Success("Confirmed. Ksh1,000 received")
        coEvery { parseAndStore(listOf("Confirmed. Ksh1,000 received"), PaymentSource.CAMERA_OCR) } returns
            Result.Success(ParseOutcome(submitted = 1, recognized = 1, stored = 1))

        val vm = viewModel()
        vm.onImagePicked("content://img")

        val state = vm.uiState.value
        assertFalse(state.isProcessing)
        assertEquals(1, state.storedCount)
        assertFalse(state.failed)
        coVerify { parseAndStore(listOf("Confirmed. Ksh1,000 received"), PaymentSource.CAMERA_OCR) }
    }

    @Test
    fun givenAllDuplicates_whenImagePicked_thenStoredCountIsZero() = runTest {
        coEvery { recognizer.recognize(any()) } returns Result.Success("Confirmed. Ksh500 received")
        coEvery { parseAndStore(any(), PaymentSource.CAMERA_OCR) } returns
            Result.Success(ParseOutcome(submitted = 1, recognized = 1, stored = 0))

        val vm = viewModel()
        vm.onImagePicked("content://img")

        assertEquals(0, vm.uiState.value.storedCount)
        assertFalse(vm.uiState.value.failed)
    }

    @Test
    fun givenRecognizerFails_whenImagePicked_thenFailedAndNoParsing() = runTest {
        coEvery { recognizer.recognize(any()) } returns
            Result.Error(AppException.LocalException("No text found in the image"))

        val vm = viewModel()
        vm.onImagePicked("content://img")

        assertTrue(vm.uiState.value.failed)
        assertFalse(vm.uiState.value.isProcessing)
        coVerify(exactly = 0) { parseAndStore(any(), any()) }
    }

    @Test
    fun givenTextHasNoPayment_whenImagePicked_thenFailed() = runTest {
        coEvery { recognizer.recognize(any()) } returns Result.Success("a shopping list")
        coEvery { parseAndStore(any(), PaymentSource.CAMERA_OCR) } returns
            Result.Error(AppException.LocalException("Could not read any payment from the message"))

        val vm = viewModel()
        vm.onImagePicked("content://img")

        assertTrue(vm.uiState.value.failed)
        assertEquals(null, vm.uiState.value.storedCount)
    }
}
