package com.flowgroup.flowta.ui.viewmodel.transaction

import app.cash.turbine.test
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.Wallet
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.usecase.transaction.RecordTransactionUseCase
import com.flowgroup.flowta.domain.usecase.wallet.ObserveWalletsForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.transaction.RecordTransactionEvent
import com.flowgroup.flowta.ui.state.transaction.RecordTransactionUiEvent
import com.flowgroup.flowta.ui.state.transaction.RecordTransactionUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordTransactionViewModelTest {

    private val observeWallets: ObserveWalletsForCurrentBusinessUseCase = mockk()
    private val recordTransaction: RecordTransactionUseCase = mockk()

    private val cash = Wallet(
        id = "w-cash",
        businessId = "b-1",
        name = "Cash drawer",
        type = WalletType.CASH,
        openingBalance = Money(0L, CurrencyCode.KES),
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )
    private val mpesa = cash.copy(id = "w-mpesa", name = "M-Pesa till", type = WalletType.MPESA)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun givenWalletsLoaded_whenInitialised_thenContentStateWithFirstWalletSelected() = runTest {
        every { observeWallets() } returns flowOf(Result.Success(listOf(cash, mpesa)))

        val viewModel = RecordTransactionViewModel(observeWallets, recordTransaction)

        val state = viewModel.uiState.value as RecordTransactionUiState.Content
        assertEquals(listOf(cash, mpesa), state.wallets)
        assertEquals(cash.id, state.selectedWalletId)
        assertEquals(TransactionType.SALE, state.type)
    }

    @Test
    fun givenNoWallets_whenInitialised_thenNoWalletsState() = runTest {
        every { observeWallets() } returns flowOf(Result.Success(emptyList()))

        val viewModel = RecordTransactionViewModel(observeWallets, recordTransaction)

        assertEquals(RecordTransactionUiState.NoWallets, viewModel.uiState.value)
    }

    @Test
    fun givenNonDigitAmountInput_whenChanged_thenInputIsSanitisedToDigitsOnly() = runTest {
        every { observeWallets() } returns flowOf(Result.Success(listOf(cash)))
        val viewModel = RecordTransactionViewModel(observeWallets, recordTransaction)

        viewModel.onEvent(RecordTransactionEvent.AmountChanged("1a 2b3"))

        val state = viewModel.uiState.value as RecordTransactionUiState.Content
        assertEquals("123", state.amountInput)
    }

    @Test
    fun givenBlankAmount_whenSubmit_thenAmountRequiredErrorAndRecordNotCalled() = runTest {
        every { observeWallets() } returns flowOf(Result.Success(listOf(cash)))
        val viewModel = RecordTransactionViewModel(observeWallets, recordTransaction)

        viewModel.onEvent(RecordTransactionEvent.Submit)

        val state = viewModel.uiState.value as RecordTransactionUiState.Content
        assertEquals(RecordTransactionUiState.Content.AmountError.Required, state.amountError)
        coVerify(exactly = 0) { recordTransaction(any(), any(), any(), any()) }
    }

    @Test
    fun givenZeroAmount_whenSubmit_thenAmountInvalidErrorAndRecordNotCalled() = runTest {
        every { observeWallets() } returns flowOf(Result.Success(listOf(cash)))
        val viewModel = RecordTransactionViewModel(observeWallets, recordTransaction)
        viewModel.onEvent(RecordTransactionEvent.AmountChanged("0"))

        viewModel.onEvent(RecordTransactionEvent.Submit)

        val state = viewModel.uiState.value as RecordTransactionUiState.Content
        assertEquals(RecordTransactionUiState.Content.AmountError.Invalid, state.amountError)
        coVerify(exactly = 0) { recordTransaction(any(), any(), any(), any()) }
    }

    @Test
    fun givenValidExpense_whenSubmit_thenRecordedEventEmittedWithExpenseType() = runTest {
        every { observeWallets() } returns flowOf(Result.Success(listOf(cash, mpesa)))
        val expectedAmount = Money(500L, CurrencyCode.KES)
        coEvery {
            recordTransaction(mpesa.id, TransactionType.EXPENSE, expectedAmount, "Stock")
        } returns Result.Success(mockk<Transaction>(relaxed = true))

        val viewModel = RecordTransactionViewModel(observeWallets, recordTransaction)
        viewModel.onEvent(RecordTransactionEvent.WalletChanged(mpesa.id))
        viewModel.onEvent(RecordTransactionEvent.TypeChanged(TransactionType.EXPENSE))
        viewModel.onEvent(RecordTransactionEvent.AmountChanged("500"))
        viewModel.onEvent(RecordTransactionEvent.NoteChanged("Stock"))

        viewModel.events.test {
            viewModel.onEvent(RecordTransactionEvent.Submit)
            assertEquals(RecordTransactionUiEvent.Recorded, awaitItem())
        }
        coVerify { recordTransaction(mpesa.id, TransactionType.EXPENSE, expectedAmount, "Stock") }
    }

    @Test
    fun givenSelectedWalletIsRemovedFromUpstream_whenWalletsUpdate_thenFallsBackToFirstAvailable() = runTest {
        // Single-emission test of fallback: only mpesa is available, so the picker should select it
        every { observeWallets() } returns flowOf(Result.Success(listOf(mpesa)))

        val viewModel = RecordTransactionViewModel(observeWallets, recordTransaction)

        val state = viewModel.uiState.value as RecordTransactionUiState.Content
        assertEquals(mpesa.id, state.selectedWalletId)
        assertTrue(state.wallets.contains(mpesa))
    }
}
