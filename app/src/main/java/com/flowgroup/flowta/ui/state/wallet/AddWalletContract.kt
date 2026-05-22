package com.flowgroup.flowta.ui.state.wallet

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.WalletType

sealed class AddWalletUiState {
    data object Loading : AddWalletUiState()
    data class Content(
        val currency: CurrencyCode,
        val type: WalletType = WalletType.CASH,
        val name: String = "",
        val openingBalanceInput: String = "",
        val nameError: NameError? = null,
        val balanceError: BalanceError? = null,
        val isSubmitting: Boolean = false,
        val submitError: String? = null,
    ) : AddWalletUiState() {

        enum class NameError(val messageRes: Int) {
            Blank(com.flowgroup.flowta.R.string.add_wallet_error_name_blank),
            TooLong(com.flowgroup.flowta.R.string.add_wallet_error_name_too_long),
        }

        enum class BalanceError(val messageRes: Int) {
            Invalid(com.flowgroup.flowta.R.string.add_wallet_error_balance_invalid),
        }
    }

    data object MissingBusiness : AddWalletUiState()
}

sealed class AddWalletEvent {
    data class TypeChanged(val type: WalletType) : AddWalletEvent()
    data class NameChanged(val name: String) : AddWalletEvent()
    data class OpeningBalanceChanged(val input: String) : AddWalletEvent()
    data object Submit : AddWalletEvent()
}

sealed class AddWalletUiEvent {
    data object Created : AddWalletUiEvent()
}
