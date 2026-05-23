package com.flowgroup.flowta.ui.viewmodel.transaction

import androidx.lifecycle.SavedStateHandle
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.TransactionWithWallet
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.usecase.transaction.DeleteTransactionUseCase
import com.flowgroup.flowta.domain.usecase.transaction.ObserveTransactionDetailUseCase
import com.flowgroup.flowta.ui.state.transaction.TransactionDetailEvent
import com.flowgroup.flowta.ui.state.transaction.TransactionDetailUiEvent
import com.flowgroup.flowta.ui.state.transaction.TransactionDetailUiState
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
class TransactionDetailViewModelTest {

    private val observeTransactionDetail: ObserveTransactionDetailUseCase = mockk()
    private val deleteTransaction: DeleteTransactionUseCase = mockk()

    private val detail = TransactionWithWallet(
        transaction = Transaction(
            id = "t-1",
            businessId = "biz-1",
            walletId = "w-1",
            type = TransactionType.SALE,
            amount = Money(12_000L, CurrencyCode.KES),
            note = "Sukari",
            occurredAt = Instant.fromEpochMilliseconds(0),
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        ),
        walletName = "Cash drawer",
        walletType = WalletType.CASH,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = TransactionDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("transactionId" to "t-1")),
        observeTransactionDetail = observeTransactionDetail,
        deleteTransaction = deleteTransaction,
    )

    @Test
    fun givenDetailEmitted_whenInitialised_thenContentState() = runTest {
        every { observeTransactionDetail("t-1") } returns flowOf(Result.Success(detail))

        val state = viewModel().uiState.value

        assertTrue(state is TransactionDetailUiState.Content)
        assertEquals(detail, (state as TransactionDetailUiState.Content).detail)
    }

    @Test
    fun givenNullDetail_whenInitialised_thenNotFoundState() = runTest {
        every { observeTransactionDetail("t-1") } returns flowOf(Result.Success(null))

        assertEquals(TransactionDetailUiState.NotFound, viewModel().uiState.value)
    }

    @Test
    fun givenError_whenInitialised_thenErrorState() = runTest {
        every { observeTransactionDetail("t-1") } returns
            flowOf(Result.Error(AppException.LocalException("boom")))

        val state = viewModel().uiState.value

        assertTrue(state is TransactionDetailUiState.Error)
        assertEquals("boom", (state as TransactionDetailUiState.Error).message)
    }

    @Test
    fun givenContent_whenDeleteConfirmed_thenDeletedEventEmitted() = runTest {
        every { observeTransactionDetail("t-1") } returns flowOf(Result.Success(detail))
        coEvery { deleteTransaction("t-1") } returns Result.Success(Unit)

        val viewModel = viewModel()
        viewModel.onEvent(TransactionDetailEvent.DeleteConfirmed)

        assertEquals(TransactionDetailUiEvent.Deleted, viewModel.events.first())
    }
}
