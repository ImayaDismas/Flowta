package com.flowgroup.flowta.ui.viewmodel.transaction

import androidx.lifecycle.SavedStateHandle
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.Wallet
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.usecase.transaction.GetTransactionUseCase
import com.flowgroup.flowta.domain.usecase.transaction.UpdateTransactionUseCase
import com.flowgroup.flowta.domain.usecase.wallet.ObserveWalletsForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.transaction.EditTransactionEvent
import com.flowgroup.flowta.ui.state.transaction.EditTransactionUiEvent
import com.flowgroup.flowta.ui.state.transaction.EditTransactionUiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class EditTransactionViewModelTest {

    private val observeWallets: ObserveWalletsForCurrentBusinessUseCase = mockk()
    private val getTransaction: GetTransactionUseCase = mockk()
    private val updateTransaction: UpdateTransactionUseCase = mockk()

    private val wallet = Wallet(
        id = "w-1",
        businessId = "biz-1",
        name = "Cash drawer",
        type = WalletType.CASH,
        openingBalance = Money(0L, CurrencyCode.KES),
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    private val transaction = Transaction(
        id = "t-1",
        businessId = "biz-1",
        walletId = "w-1",
        type = TransactionType.EXPENSE,
        amount = Money(500L, CurrencyCode.KES),
        note = "rent",
        occurredAt = Instant.fromEpochMilliseconds(0),
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = EditTransactionViewModel(
        savedStateHandle = SavedStateHandle(mapOf("transactionId" to "t-1")),
        observeWallets = observeWallets,
        getTransaction = getTransaction,
        updateTransaction = updateTransaction,
    )

    @Test
    fun givenExistingTransaction_whenInitialised_thenFormPrefilled() = runTest {
        coEvery { getTransaction("t-1") } returns Result.Success(transaction)
        every { observeWallets() } returns flowOf(Result.Success(listOf(wallet)))

        val state = viewModel().uiState.value

        assertTrue(state is EditTransactionUiState.Content)
        state as EditTransactionUiState.Content
        assertEquals("w-1", state.selectedWalletId)
        assertEquals(TransactionType.EXPENSE, state.type)
        assertEquals("500", state.amountInput)
        assertEquals("rent", state.note)
    }

    @Test
    fun givenMissingTransaction_whenInitialised_thenNotFound() = runTest {
        coEvery { getTransaction("t-1") } returns Result.Success(null)
        every { observeWallets() } returns flowOf(Result.Success(listOf(wallet)))

        assertEquals(EditTransactionUiState.NotFound, viewModel().uiState.value)
    }

    @Test
    fun givenLoadedForm_whenSaved_thenSavedEventEmitted() = runTest {
        coEvery { getTransaction("t-1") } returns Result.Success(transaction)
        every { observeWallets() } returns flowOf(Result.Success(listOf(wallet)))
        coEvery {
            updateTransaction("t-1", "w-1", TransactionType.EXPENSE, Money(500L, CurrencyCode.KES), "rent")
        } returns Result.Success(Unit)

        val viewModel = viewModel()
        viewModel.onEvent(EditTransactionEvent.Save)

        assertEquals(EditTransactionUiEvent.Saved, viewModel.events.first())
    }
}
