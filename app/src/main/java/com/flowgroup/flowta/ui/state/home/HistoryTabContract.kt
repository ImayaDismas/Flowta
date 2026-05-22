package com.flowgroup.flowta.ui.state.home

import com.flowgroup.flowta.domain.model.TransactionWithWallet

sealed class HistoryTabUiState {
    data object Loading : HistoryTabUiState()
    data class Content(val items: List<TransactionWithWallet>) : HistoryTabUiState()
    data class Error(val message: String) : HistoryTabUiState()
}
