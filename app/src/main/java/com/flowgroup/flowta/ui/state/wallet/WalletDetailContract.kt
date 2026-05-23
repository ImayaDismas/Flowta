package com.flowgroup.flowta.ui.state.wallet

import com.flowgroup.flowta.domain.model.WalletDetail

sealed class WalletDetailUiState {
    data object Loading : WalletDetailUiState()
    data object NotFound : WalletDetailUiState()
    data class Content(val detail: WalletDetail) : WalletDetailUiState()
    data class Error(val message: String) : WalletDetailUiState()
}
