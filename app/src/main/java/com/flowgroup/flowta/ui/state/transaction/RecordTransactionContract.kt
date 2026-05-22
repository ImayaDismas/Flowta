package com.flowgroup.flowta.ui.state.transaction

import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.Wallet

sealed class RecordTransactionUiState {
    data object Loading : RecordTransactionUiState()
    data object NoWallets : RecordTransactionUiState()
    data class Content(
        val wallets: List<Wallet>,
        val selectedWalletId: String,
        val type: TransactionType = TransactionType.SALE,
        val amountInput: String = "",
        val note: String = "",
        val isSubmitting: Boolean = false,
        val amountError: AmountError? = null,
        val submitError: String? = null,
    ) : RecordTransactionUiState() {

        val selectedWallet: Wallet
            get() = wallets.first { it.id == selectedWalletId }

        enum class AmountError(val messageRes: Int) {
            Required(com.flowgroup.flowta.R.string.record_tx_error_amount_required),
            Invalid(com.flowgroup.flowta.R.string.record_tx_error_amount_invalid),
        }
    }
}

sealed class RecordTransactionEvent {
    data class TypeChanged(val type: TransactionType) : RecordTransactionEvent()
    data class WalletChanged(val walletId: String) : RecordTransactionEvent()
    data class AmountChanged(val input: String) : RecordTransactionEvent()
    data class NoteChanged(val note: String) : RecordTransactionEvent()
    data object Submit : RecordTransactionEvent()
}

sealed class RecordTransactionUiEvent {
    data object Recorded : RecordTransactionUiEvent()
}
