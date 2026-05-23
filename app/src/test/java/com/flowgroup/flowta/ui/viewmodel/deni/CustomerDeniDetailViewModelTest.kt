package com.flowgroup.flowta.ui.viewmodel.deni

import androidx.lifecycle.SavedStateHandle
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Customer
import com.flowgroup.flowta.domain.model.CustomerDeniDetail
import com.flowgroup.flowta.domain.usecase.deni.ObserveCustomerDeniUseCase
import com.flowgroup.flowta.domain.usecase.deni.RecordDeniCreditUseCase
import com.flowgroup.flowta.domain.usecase.deni.RecordDeniPaymentUseCase
import com.flowgroup.flowta.ui.state.deni.CustomerDeniDetailEvent
import com.flowgroup.flowta.ui.state.deni.CustomerDeniDetailUiState
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
class CustomerDeniDetailViewModelTest {

    private val observeCustomerDeni: ObserveCustomerDeniUseCase = mockk()
    private val recordCredit: RecordDeniCreditUseCase = mockk()
    private val recordPayment: RecordDeniPaymentUseCase = mockk()

    private val detail = CustomerDeniDetail(
        customer = Customer(
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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = CustomerDeniDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("customerId" to "c-1")),
        observeCustomerDeni = observeCustomerDeni,
        recordCredit = recordCredit,
        recordPayment = recordPayment,
    )

    @Test
    fun givenDetailEmitted_whenInitialised_thenContent() = runTest {
        every { observeCustomerDeni("c-1") } returns flowOf(Result.Success(detail))

        val state = viewModel().uiState.value

        assertTrue(state is CustomerDeniDetailUiState.Content)
        assertEquals(detail, (state as CustomerDeniDetailUiState.Content).detail)
    }

    @Test
    fun givenPaymentDialog_whenConfirmed_thenPaymentRecordedAndDialogClosed() = runTest {
        every { observeCustomerDeni("c-1") } returns flowOf(Result.Success(detail))
        coEvery { recordPayment("c-1", 300L, "") } returns Result.Success(mockk())

        val viewModel = viewModel()
        viewModel.onEvent(CustomerDeniDetailEvent.RecordPaymentClicked)
        viewModel.onEvent(CustomerDeniDetailEvent.AmountChanged("300"))
        viewModel.onEvent(CustomerDeniDetailEvent.DialogConfirmed)

        coVerify { recordPayment("c-1", 300L, "") }
        val state = viewModel.uiState.value as CustomerDeniDetailUiState.Content
        assertNull(state.dialog)
    }

    @Test
    fun givenZeroAmount_whenConfirmed_thenAmountErrorAndNotRecorded() = runTest {
        every { observeCustomerDeni("c-1") } returns flowOf(Result.Success(detail))

        val viewModel = viewModel()
        viewModel.onEvent(CustomerDeniDetailEvent.AddCreditClicked)
        viewModel.onEvent(CustomerDeniDetailEvent.DialogConfirmed)

        val state = viewModel.uiState.value as CustomerDeniDetailUiState.Content
        assertTrue(state.amountError)
        coVerify(exactly = 0) { recordCredit(any(), any(), any()) }
    }
}
