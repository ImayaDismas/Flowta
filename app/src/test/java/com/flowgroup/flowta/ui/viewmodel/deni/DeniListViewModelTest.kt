package com.flowgroup.flowta.ui.viewmodel.deni

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Customer
import com.flowgroup.flowta.domain.model.CustomerDeni
import com.flowgroup.flowta.domain.usecase.deni.ObserveCustomersDeniForCurrentBusinessUseCase
import com.flowgroup.flowta.domain.usecase.deni.ObserveTotalOutstandingForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.deni.DeniListUiState
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
class DeniListViewModelTest {

    private val observeCustomers: ObserveCustomersDeniForCurrentBusinessUseCase = mockk()
    private val observeTotal: ObserveTotalOutstandingForCurrentBusinessUseCase = mockk()

    private val customerDeni = CustomerDeni(
        customer = Customer(
            id = "c-1",
            businessId = "biz-1",
            name = "Mama Achieng",
            phone = "0712345678",
            currency = CurrencyCode.KES,
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        ),
        outstandingMinor = 1_500L,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = DeniListViewModel(observeCustomers, observeTotal)

    @Test
    fun givenCustomers_whenInitialised_thenContentWithTotalAndCurrency() = runTest {
        every { observeCustomers() } returns flowOf(Result.Success(listOf(customerDeni)))
        every { observeTotal() } returns flowOf(Result.Success(1_500L))

        val state = viewModel().uiState.value

        assertTrue(state is DeniListUiState.Content)
        state as DeniListUiState.Content
        assertEquals(1_500L, state.totalOutstandingMinor)
        assertEquals(CurrencyCode.KES, state.currency)
        assertEquals(1, state.customers.size)
    }

    @Test
    fun givenNoCustomers_whenInitialised_thenEmpty() = runTest {
        every { observeCustomers() } returns flowOf(Result.Success(emptyList()))
        every { observeTotal() } returns flowOf(Result.Success(0L))

        assertEquals(DeniListUiState.Empty, viewModel().uiState.value)
    }

    @Test
    fun givenError_whenInitialised_thenError() = runTest {
        every { observeCustomers() } returns flowOf(Result.Error(AppException.LocalException("boom")))
        every { observeTotal() } returns flowOf(Result.Success(0L))

        val state = viewModel().uiState.value

        assertTrue(state is DeniListUiState.Error)
        assertEquals("boom", (state as DeniListUiState.Error).message)
    }
}
