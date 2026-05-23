package com.flowgroup.flowta.ui.state.wallet

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.WalletType

sealed class EditWalletUiState {
    data object Loading : EditWalletUiState()
    data object NotFound : EditWalletUiState()
    data class Content(
        val walletId: String,
        val currency: CurrencyCode,
        val openingBalanceMinor: Long,
        val name: String,
        val type: WalletType,
        val nameError: NameError? = null,
        val isSaving: Boolean = false,
        val isDeleting: Boolean = false,
        val submitError: String? = null,
        val deleteDialog: DeleteDialog? = null,
    ) : EditWalletUiState() {

        enum class NameError(val messageRes: Int) {
            Blank(com.flowgroup.flowta.R.string.edit_wallet_error_name_blank),
            TooLong(com.flowgroup.flowta.R.string.edit_wallet_error_name_too_long),
        }

        sealed class DeleteDialog {
            data object Confirm : DeleteDialog()
            data class Blocked(val transactionCount: Int) : DeleteDialog()
        }
    }
}

sealed class EditWalletEvent {
    data class NameChanged(val name: String) : EditWalletEvent()
    data class TypeChanged(val type: WalletType) : EditWalletEvent()
    data object Save : EditWalletEvent()
    data object DeleteRequested : EditWalletEvent()
    data object DeleteConfirmed : EditWalletEvent()
    data object DismissDialog : EditWalletEvent()
}

sealed class EditWalletUiEvent {
    data object Saved : EditWalletUiEvent()
    data object Deleted : EditWalletUiEvent()
}
