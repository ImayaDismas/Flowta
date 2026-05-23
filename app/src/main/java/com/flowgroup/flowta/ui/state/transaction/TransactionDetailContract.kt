package com.flowgroup.flowta.ui.state.transaction

import com.flowgroup.flowta.domain.model.TransactionWithWallet

sealed class TransactionDetailUiState {
    data object Loading : TransactionDetailUiState()
    data object NotFound : TransactionDetailUiState()
    data class Content(
        val detail: TransactionWithWallet,
        val confirmingDelete: Boolean = false,
        val isDeleting: Boolean = false,
    ) : TransactionDetailUiState()
    data class Error(val message: String) : TransactionDetailUiState()
}

sealed class TransactionDetailEvent {
    data object DeleteRequested : TransactionDetailEvent()
    data object DeleteConfirmed : TransactionDetailEvent()
    data object DeleteDismissed : TransactionDetailEvent()
}

sealed class TransactionDetailUiEvent {
    data object Deleted : TransactionDetailUiEvent()
}
