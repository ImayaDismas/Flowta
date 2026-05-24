package com.flowgroup.flowta.ui.viewmodel.deni

import androidx.lifecycle.SavedStateHandle
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Client
import com.flowgroup.flowta.domain.model.ClientDeniDetail
import com.flowgroup.flowta.domain.model.WalletWithBalance
import com.flowgroup.flowta.domain.usecase.deni.ObserveClientDeniUseCase
import com.flowgroup.flowta.domain.usecase.deni.RecordDeniCreditUseCase
import com.flowgroup.flowta.domain.usecase.deni.RecordDeniPaymentUseCase
import com.flowgroup.flowta.domain.usecase.wallet.ObserveWalletsWithBalanceForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.deni.ClientDeniDetailEvent
import com.flowgroup.flowta.ui.state.deni.ClientDeniDetailUiState
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientDeniDetailViewModelTest {

    private val observeClientDeni: ObserveClientDeniUseCase = mockk()
    private val observeWallets: ObserveWalletsWithBalanceForCurrentBusinessUseCase = mockk()
    private val recordCredit: RecordDeniCreditUseCase = mockk()
    private val recordPayment: RecordDeniPaymentUseCase = mockk()

    private val detail = ClientDeniDetail(
        client = Client(
            id = "c-1",
            businessId = "biz-1",
            name = "Mama Achieng",
            phone = null,
            currency = CurrencyCode.KES,
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        ),
        outstandingMinor = 1_500L,
        entries = emptyList(),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { observeWallets() } returns flowOf(Result.Success(emptyList<WalletWithBalance>()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ClientDeniDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("clientId" to "c-1")),
        observeClientDeni = observeClientDeni,
        observeWallets = observeWallets,
        recordCredit = recordCredit,
        recordPayment = recordPayment,
    )

    @Test
    fun givenDetailEmitted_whenInitialised_thenContent() = runTest {
        every { observeClientDeni("c-1") } returns flowOf(Result.Success(detail))

        val state = viewModel().uiState.value

        assertTrue(state is ClientDeniDetailUiState.Content)
        assertEquals(detail, (state as ClientDeniDetailUiState.Content).detail)
    }

    @Test
    fun givenPaymentDialog_whenConfirmed_thenPaymentRecordedAndDialogClosed() = runTest {
        every { observeClientDeni("c-1") } returns flowOf(Result.Success(detail))
        coEvery { recordPayment("c-1", 300L, "", null) } returns Result.Success(mockk())

        val viewModel = viewModel()
        viewModel.onEvent(ClientDeniDetailEvent.RecordPaymentClicked)
        viewModel.onEvent(ClientDeniDetailEvent.AmountChanged("300"))
        viewModel.onEvent(ClientDeniDetailEvent.DialogConfirmed)

        coVerify { recordPayment("c-1", 300L, "", null) }
        val state = viewModel.uiState.value as ClientDeniDetailUiState.Content
        assertNull(state.dialog)
    }

    @Test
    fun givenZeroAmount_whenConfirmed_thenAmountErrorAndNotRecorded() = runTest {
        every { observeClientDeni("c-1") } returns flowOf(Result.Success(detail))

        val viewModel = viewModel()
        viewModel.onEvent(ClientDeniDetailEvent.AddCreditClicked)
        viewModel.onEvent(ClientDeniDetailEvent.DialogConfirmed)

        val state = viewModel.uiState.value as ClientDeniDetailUiState.Content
        assertTrue(state.amountError)
        coVerify(exactly = 0) { recordCredit(any(), any(), any(), any()) }
    }
}
