package com.flowgroup.flowta.ui.state.home

import com.flowgroup.flowta.domain.model.WalletWithBalance

sealed class WalletsTabUiState {
    data object Loading : WalletsTabUiState()
    data class Content(val items: List<WalletWithBalance>) : WalletsTabUiState()
    data class Error(val message: String) : WalletsTabUiState()
}
