package com.flowgroup.flowta.ui.viewmodel.wallet

import androidx.lifecycle.SavedStateHandle
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.TransactionTotals
import com.flowgroup.flowta.domain.model.Wallet
import com.flowgroup.flowta.domain.model.WalletDetail
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.usecase.wallet.ObserveWalletDetailUseCase
import com.flowgroup.flowta.ui.state.wallet.WalletDetailUiState
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
class WalletDetailViewModelTest {

    private val observeWalletDetail: ObserveWalletDetailUseCase = mockk()

    private val wallet = Wallet(
        id = "w-1",
        businessId = "biz-1",
        name = "Cash drawer",
        type = WalletType.CASH,
        openingBalance = Money(0L, CurrencyCode.KES),
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

    @Test
    fun givenDetailEmitted_whenInitialised_thenContentStateExposesIt() = runTest {
        val detail = WalletDetail(
            wallet = wallet,
            currentBalanceMinor = 12_000L,
            recentTransactions = emptyList(),
            weekTotals = TransactionTotals.ZERO,
        )
        every { observeWalletDetail("w-1") } returns flowOf(Result.Success(detail))

        val viewModel = WalletDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("walletId" to "w-1")),
            observeWalletDetail = observeWalletDetail,
        )

        val state = viewModel.uiState.value
        assertTrue(state is WalletDetailUiState.Content)
        assertEquals(detail, (state as WalletDetailUiState.Content).detail)
    }

    @Test
    fun givenNullDetail_whenInitialised_thenNotFoundState() = runTest {
        every { observeWalletDetail("w-1") } returns flowOf(Result.Success(null))

        val viewModel = WalletDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("walletId" to "w-1")),
            observeWalletDetail = observeWalletDetail,
        )

        assertEquals(WalletDetailUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun givenError_whenInitialised_thenErrorState() = runTest {
        every { observeWalletDetail("w-1") } returns
            flowOf(Result.Error(AppException.LocalException("boom")))

        val viewModel = WalletDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("walletId" to "w-1")),
            observeWalletDetail = observeWalletDetail,
        )

        val state = viewModel.uiState.value
        assertTrue(state is WalletDetailUiState.Error)
        assertEquals("boom", (state as WalletDetailUiState.Error).message)
    }
}
