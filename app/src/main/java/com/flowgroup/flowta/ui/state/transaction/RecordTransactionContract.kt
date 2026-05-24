package com.flowgroup.flowta.ui.state.transaction

import com.flowgroup.flowta.domain.model.ClientDeni
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
        // Credit (deni) integration — only relevant when type == SALE.
        val clients: List<ClientDeni> = emptyList(),
        val onCredit: Boolean = false,
        val creditAmountInput: String = "",
        val selectedClientId: String? = null,
        val addingNewClient: Boolean = false,
        val newClientName: String = "",
        val newClientPhone: String = "",
        val creditError: CreditError? = null,
        val clientError: Boolean = false,
    ) : RecordTransactionUiState() {

        val selectedWallet: Wallet
            get() = wallets.first { it.id == selectedWalletId }

        val selectedClientName: String?
            get() = selectedClientId?.let { id -> clients.firstOrNull { it.client.id == id }?.client?.name }

        enum class AmountError(val messageRes: Int) {
            Required(com.flowgroup.flowta.R.string.record_tx_error_amount_required),
            Invalid(com.flowgroup.flowta.R.string.record_tx_error_amount_invalid),
        }

        enum class CreditError(val messageRes: Int) {
            Required(com.flowgroup.flowta.R.string.record_tx_error_credit_required),
            Invalid(com.flowgroup.flowta.R.string.record_tx_error_amount_invalid),
            Exceeds(com.flowgroup.flowta.R.string.record_tx_error_credit_exceeds),
        }
    }
}

sealed class RecordTransactionEvent {
    data class TypeChanged(val type: TransactionType) : RecordTransactionEvent()
    data class WalletChanged(val walletId: String) : RecordTransactionEvent()
    data class AmountChanged(val input: String) : RecordTransactionEvent()
    data class NoteChanged(val note: String) : RecordTransactionEvent()
    data class CreditToggled(val onCredit: Boolean) : RecordTransactionEvent()
    data class CreditAmountChanged(val input: String) : RecordTransactionEvent()
    data class ClientSelected(val clientId: String) : RecordTransactionEvent()
    data object NewClientSelected : RecordTransactionEvent()
    data class NewClientNameChanged(val name: String) : RecordTransactionEvent()
    data class NewClientPhoneChanged(val phone: String) : RecordTransactionEvent()
    data object Submit : RecordTransactionEvent()
}

sealed class RecordTransactionUiEvent {
    data object Recorded : RecordTransactionUiEvent()
}
