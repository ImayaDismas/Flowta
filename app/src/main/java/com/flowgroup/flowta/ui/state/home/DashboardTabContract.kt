package com.flowgroup.flowta.ui.state.home

import com.flowgroup.flowta.domain.model.BusinessHealth
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.WalletWithBalance

sealed class DashboardTabUiState {
    data object Loading : DashboardTabUiState()
    data object NoBusiness : DashboardTabUiState()
    data class Content(
        val businessName: String,
        val currency: CurrencyCode,
        val health: BusinessHealth,
        val walletPreview: List<WalletWithBalance>,
        val totalWalletCount: Int,
    ) : DashboardTabUiState()
    data class Error(val message: String) : DashboardTabUiState()
}
