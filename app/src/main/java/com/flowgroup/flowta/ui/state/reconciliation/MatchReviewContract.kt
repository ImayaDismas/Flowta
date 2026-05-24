package com.flowgroup.flowta.ui.state.reconciliation

import com.flowgroup.flowta.domain.model.ReceivedPayment
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.WalletWithBalance

sealed class MatchReviewUiState {
    data object Loading : MatchReviewUiState()

    data class Content(
        val payment: ReceivedPayment,
        val suggestion: Transaction?,
        /** Hidden after the user taps "Not a match", revealing the record-as-sale path. */
        val showSuggestion: Boolean,
        val wallets: List<WalletWithBalance>,
        val selectedWalletId: String?,
        val working: Boolean = false,
        /** When true, the manual transaction picker sheet is open. */
        val showTransactionPicker: Boolean = false,
        val pickableTransactions: List<Transaction> = emptyList(),
    ) : MatchReviewUiState()

    data class Error(val message: String) : MatchReviewUiState()

    /** Match confirmed, sale recorded, or payment dismissed — screen should pop. */
    data object Done : MatchReviewUiState()
}
