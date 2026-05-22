package com.flowgroup.flowta.ui.viewmodel.wallet

import app.cash.turbine.test
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Business
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Wallet
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.usecase.business.ObserveCurrentBusinessUseCase
import com.flowgroup.flowta.domain.usecase.wallet.CreateWalletUseCase
import com.flowgroup.flowta.ui.state.wallet.AddWalletEvent
import com.flowgroup.flowta.ui.state.wallet.AddWalletUiEvent
import com.flowgroup.flowta.ui.state.wallet.AddWalletUiState
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddWalletViewModelTest {

    private val observeCurrentBusiness: ObserveCurrentBusinessUseCase = mockk()
    private val createWallet: CreateWalletUseCase = mockk()

    private val business = Business(
        id = "business-1",
        name = "Mama Lucy Kiosk",
        currency = CurrencyCode.KES,
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { observeCurrentBusiness() } returns flowOf(Result.Success(business))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun givenBusinessLoaded_whenInitialised_thenContentExposesBusinessCurrency() = runTest {
        val viewModel = AddWalletViewModel(observeCurrentBusiness, createWallet)

        val state = viewModel.uiState.value
        assertTrue(state is AddWalletUiState.Content)
        assertEquals(CurrencyCode.KES, (state as AddWalletUiState.Content).currency)
    }

    @Test
    fun givenBlankName_whenSubmit_thenNameErrorBlankAndCreateNotCalled() = runTest {
        val viewModel = AddWalletViewModel(observeCurrentBusiness, createWallet)
        viewModel.onEvent(AddWalletEvent.OpeningBalanceChanged("100"))

        viewModel.onEvent(AddWalletEvent.Submit)

        val state = viewModel.uiState.value as AddWalletUiState.Content
        assertEquals(AddWalletUiState.Content.NameError.Blank, state.nameError)
        coVerify(exactly = 0) { createWallet(any(), any(), any()) }
    }

    @Test
    fun givenNonDigitBalanceInput_whenChanged_thenInputIsSanitisedToDigitsOnly() = runTest {
        val viewModel = AddWalletViewModel(observeCurrentBusiness, createWallet)

        viewModel.onEvent(AddWalletEvent.OpeningBalanceChanged("1a 2b3"))

        val state = viewModel.uiState.value as AddWalletUiState.Content
        assertEquals("123", state.openingBalanceInput)
    }

    @Test
    fun givenValidForm_whenSubmit_thenCreatedEventIsEmitted() = runTest {
        val created = Wallet(
            id = "wallet-1",
            businessId = business.id,
            name = "Cash drawer",
            type = WalletType.CASH,
            openingBalance = Money(minorUnits = 1500L, currency = CurrencyCode.KES),
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )
        coEvery {
            createWallet("Cash drawer", WalletType.CASH, Money(1500L, CurrencyCode.KES))
        } returns Result.Success(created)

        val viewModel = AddWalletViewModel(observeCurrentBusiness, createWallet)
        viewModel.onEvent(AddWalletEvent.NameChanged("Cash drawer"))
        viewModel.onEvent(AddWalletEvent.OpeningBalanceChanged("1500"))

        viewModel.events.test {
            viewModel.onEvent(AddWalletEvent.Submit)
            assertEquals(AddWalletUiEvent.Created, awaitItem())
        }
    }

    @Test
    fun givenEmptyOpeningBalance_whenSubmit_thenItDefaultsToZeroAndSubmits() = runTest {
        val zero = Money(0L, CurrencyCode.KES)
        val created = Wallet(
            id = "wallet-2",
            businessId = business.id,
            name = "Cash",
            type = WalletType.CASH,
            openingBalance = zero,
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )
        coEvery {
            createWallet("Cash", WalletType.CASH, zero)
        } returns Result.Success(created)

        val viewModel = AddWalletViewModel(observeCurrentBusiness, createWallet)
        viewModel.onEvent(AddWalletEvent.NameChanged("Cash"))

        viewModel.onEvent(AddWalletEvent.Submit)

        coVerify { createWallet("Cash", WalletType.CASH, zero) }
        val state = viewModel.uiState.value as AddWalletUiState.Content
        assertEquals(null, state.balanceError)
    }

    @Test
    fun givenTypeChangedToMpesa_whenSubmittedWithValidName_thenCreatesMpesaWallet() = runTest {
        val opening = Money(0L, CurrencyCode.KES)
        coEvery {
            createWallet("Till 12345", WalletType.MPESA, opening)
        } returns Result.Success(mockk(relaxed = true))

        val viewModel = AddWalletViewModel(observeCurrentBusiness, createWallet)
        viewModel.onEvent(AddWalletEvent.TypeChanged(WalletType.MPESA))
        viewModel.onEvent(AddWalletEvent.NameChanged("Till 12345"))
        viewModel.onEvent(AddWalletEvent.Submit)

        coVerify { createWallet("Till 12345", WalletType.MPESA, opening) }
        assertNotNull(viewModel.uiState.value)
    }
}
