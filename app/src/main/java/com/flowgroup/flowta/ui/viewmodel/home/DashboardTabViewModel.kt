package com.flowgroup.flowta.ui.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Business
import com.flowgroup.flowta.domain.model.BusinessHealth
import com.flowgroup.flowta.domain.model.HealthPeriod
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.WalletWithBalance
import com.flowgroup.flowta.domain.usecase.business.ObserveCurrentBusinessUseCase
import com.flowgroup.flowta.domain.usecase.deni.ObserveTotalOutstandingForCurrentBusinessUseCase
import com.flowgroup.flowta.domain.usecase.transaction.ObserveBusinessHealthForCurrentBusinessUseCase
import com.flowgroup.flowta.domain.usecase.wallet.ObserveWalletsWithBalanceForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.home.DashboardTabUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardTabViewModel @Inject constructor(
    observeCurrentBusiness: ObserveCurrentBusinessUseCase,
    observeWallets: ObserveWalletsWithBalanceForCurrentBusinessUseCase,
    observeHealth: ObserveBusinessHealthForCurrentBusinessUseCase,
    observeOutstandingDeni: ObserveTotalOutstandingForCurrentBusinessUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardTabUiState>(DashboardTabUiState.Loading)
    val uiState: StateFlow<DashboardTabUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeCurrentBusiness(),
                observeWallets(),
                observeHealth(),
                observeOutstandingDeni(),
            ) { businessResult, walletsResult, healthResult, deniResult ->
                mapToUiState(businessResult, walletsResult, healthResult, deniResult)
            }.collect { _uiState.value = it }
        }
    }

    private fun mapToUiState(
        businessResult: Result<Business?>,
        walletsResult: Result<List<WalletWithBalance>>,
        healthResult: Result<BusinessHealth?>,
        deniResult: Result<Long>,
    ): DashboardTabUiState {
        if (businessResult is Result.Error) {
            return DashboardTabUiState.Error(businessResult.exception.message.orEmpty())
        }
        if (walletsResult is Result.Error) {
            return DashboardTabUiState.Error(walletsResult.exception.message.orEmpty())
        }
        if (healthResult is Result.Error) {
            return DashboardTabUiState.Error(healthResult.exception.message.orEmpty())
        }
        if (deniResult is Result.Error) {
            return DashboardTabUiState.Error(deniResult.exception.message.orEmpty())
        }
        val business = (businessResult as Result.Success).data
            ?: return DashboardTabUiState.NoBusiness
        val wallets = (walletsResult as Result.Success).data
        val health = (healthResult as Result.Success).data ?: BusinessHealth(
            period = HealthPeriod.THIS_WEEK,
            revenue = Money(0L, business.currency),
            expenses = Money(0L, business.currency),
            revenueDeltaPercent = null,
            expensesDeltaPercent = null,
        )
        return DashboardTabUiState.Content(
            businessName = business.name,
            currency = business.currency,
            health = health,
            walletPreview = wallets.take(WALLET_PREVIEW_LIMIT),
            totalWalletCount = wallets.size,
            outstandingDeniMinor = (deniResult as Result.Success).data,
        )
    }

    private companion object {
        const val WALLET_PREVIEW_LIMIT = 5
    }
}
