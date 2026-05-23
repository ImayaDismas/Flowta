package com.flowgroup.flowta.ui.state.transaction

import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.Wallet

sealed class EditTransactionUiState {
    data object Loading : EditTransactionUiState()
    data object NotFound : EditTransactionUiState()
    data class Content(
        val wallets: List<Wallet>,
        val selectedWalletId: String,
        val type: TransactionType,
        val amountInput: String,
        val note: String,
        val isSaving: Boolean = false,
        val amountError: AmountError? = null,
        val submitError: String? = null,
    ) : EditTransactionUiState() {

        val selectedWallet: Wallet
            get() = wallets.first { it.id == selectedWalletId }

        enum class AmountError(val messageRes: Int) {
            Required(com.flowgroup.flowta.R.string.record_tx_error_amount_required),
            Invalid(com.flowgroup.flowta.R.string.record_tx_error_amount_invalid),
        }
    }
}

sealed class EditTransactionEvent {
    data class TypeChanged(val type: TransactionType) : EditTransactionEvent()
    data class WalletChanged(val walletId: String) : EditTransactionEvent()
    data class AmountChanged(val input: String) : EditTransactionEvent()
    data class NoteChanged(val note: String) : EditTransactionEvent()
    data object Save : EditTransactionEvent()
}

sealed class EditTransactionUiEvent {
    data object Saved : EditTransactionUiEvent()
}
