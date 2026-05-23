package com.flowgroup.flowta.ui.viewmodel.home

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Business
import com.flowgroup.flowta.domain.model.BusinessHealth
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.HealthPeriod
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Wallet
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.model.WalletWithBalance
import com.flowgroup.flowta.domain.usecase.business.ObserveCurrentBusinessUseCase
import com.flowgroup.flowta.domain.usecase.deni.ObserveTotalOutstandingForCurrentBusinessUseCase
import com.flowgroup.flowta.domain.usecase.transaction.ObserveBusinessHealthForCurrentBusinessUseCase
import com.flowgroup.flowta.domain.usecase.wallet.ObserveWalletsWithBalanceForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.home.DashboardTabUiState
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
class DashboardTabViewModelTest {

    private val observeCurrentBusiness: ObserveCurrentBusinessUseCase = mockk()
    private val observeWallets: ObserveWalletsWithBalanceForCurrentBusinessUseCase = mockk()
    private val observeHealth: ObserveBusinessHealthForCurrentBusinessUseCase = mockk()
    private val observeOutstandingDeni: ObserveTotalOutstandingForCurrentBusinessUseCase = mockk()

    private val business = Business(
        id = "biz-1",
        name = "Mama Lucy Kiosk",
        currency = CurrencyCode.KES,
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    private val cash = WalletWithBalance(
        wallet = Wallet(
            id = "w-cash",
            businessId = "biz-1",
            name = "Cash drawer",
            type = WalletType.CASH,
            openingBalance = Money(0L, CurrencyCode.KES),
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        ),
        currentBalanceMinor = 10_000L,
    )

    private val health = BusinessHealth(
        period = HealthPeriod.THIS_WEEK,
        revenue = Money(50_000L, CurrencyCode.KES),
        expenses = Money(20_000L, CurrencyCode.KES),
        revenueDeltaPercent = 12.5,
        expensesDeltaPercent = -4.0,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { observeOutstandingDeni() } returns flowOf(Result.Success(0L))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun givenAllSucceed_whenInitialised_thenContentEmitted() = runTest {
        every { observeCurrentBusiness() } returns flowOf(Result.Success(business))
        every { observeWallets() } returns flowOf(Result.Success(listOf(cash)))
        every { observeHealth() } returns flowOf(Result.Success(health))

        val viewModel = DashboardTabViewModel(observeCurrentBusiness, observeWallets, observeHealth, observeOutstandingDeni)

        val state = viewModel.uiState.value
        assertTrue(state is DashboardTabUiState.Content)
        val content = state as DashboardTabUiState.Content
        assertEquals("Mama Lucy Kiosk", content.businessName)
        assertEquals(CurrencyCode.KES, content.currency)
        assertEquals(health, content.health)
        assertEquals(listOf(cash), content.walletPreview)
        assertEquals(1, content.totalWalletCount)
    }

    @Test
    fun givenNoCurrentBusiness_whenInitialised_thenNoBusinessEmitted() = runTest {
        every { observeCurrentBusiness() } returns flowOf(Result.Success(null))
        every { observeWallets() } returns flowOf(Result.Success(emptyList()))
        every { observeHealth() } returns flowOf(Result.Success(null))

        val viewModel = DashboardTabViewModel(observeCurrentBusiness, observeWallets, observeHealth, observeOutstandingDeni)

        assertEquals(DashboardTabUiState.NoBusiness, viewModel.uiState.value)
    }

    @Test
    fun givenNoTransactions_whenInitialised_thenContentWithZeroHealth() = runTest {
        every { observeCurrentBusiness() } returns flowOf(Result.Success(business))
        every { observeWallets() } returns flowOf(Result.Success(emptyList()))
        every { observeHealth() } returns flowOf(Result.Success(null))

        val viewModel = DashboardTabViewModel(observeCurrentBusiness, observeWallets, observeHealth, observeOutstandingDeni)

        val content = viewModel.uiState.value as DashboardTabUiState.Content
        assertEquals(0L, content.health.revenue.minorUnits)
        assertEquals(0L, content.health.expenses.minorUnits)
        assertNull(content.health.revenueDeltaPercent)
        assertNull(content.health.expensesDeltaPercent)
        assertEquals(emptyList<WalletWithBalance>(), content.walletPreview)
    }

    @Test
    fun givenBusinessRepoError_whenInitialised_thenErrorEmitted() = runTest {
        every { observeCurrentBusiness() } returns
            flowOf(Result.Error(AppException.LocalException("db down")))
        every { observeWallets() } returns flowOf(Result.Success(emptyList()))
        every { observeHealth() } returns flowOf(Result.Success(null))

        val viewModel = DashboardTabViewModel(observeCurrentBusiness, observeWallets, observeHealth, observeOutstandingDeni)

        val state = viewModel.uiState.value
        assertTrue(state is DashboardTabUiState.Error)
        assertEquals("db down", (state as DashboardTabUiState.Error).message)
    }

    @Test
    fun givenManyWallets_whenInitialised_thenPreviewCappedAtFive() = runTest {
        val many = (1..7).map { idx ->
            cash.copy(wallet = cash.wallet.copy(id = "w-$idx", name = "Wallet $idx"))
        }
        every { observeCurrentBusiness() } returns flowOf(Result.Success(business))
        every { observeWallets() } returns flowOf(Result.Success(many))
        every { observeHealth() } returns flowOf(Result.Success(health))

        val viewModel = DashboardTabViewModel(observeCurrentBusiness, observeWallets, observeHealth, observeOutstandingDeni)

        val content = viewModel.uiState.value as DashboardTabUiState.Content
        assertEquals(5, content.walletPreview.size)
        assertEquals(7, content.totalWalletCount)
    }
}
