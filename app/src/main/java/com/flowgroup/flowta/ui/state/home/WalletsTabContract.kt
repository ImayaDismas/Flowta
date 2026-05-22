package com.flowgroup.flowta.ui.state.home

import com.flowgroup.flowta.domain.model.Wallet

sealed class WalletsTabUiState {
    data object Loading : WalletsTabUiState()
    data class Content(val wallets: List<Wallet>) : WalletsTabUiState()
    data class Error(val message: String) : WalletsTabUiState()
}
