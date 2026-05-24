package com.flowgroup.flowta.ui.state.deni

import com.flowgroup.flowta.domain.model.WalletWithBalance

data class AddClientUiState(
    val name: String = "",
    val phone: String = "",
    val initialCreditInput: String = "",
    val wallets: List<WalletWithBalance> = emptyList(),
    val selectedWalletId: String? = null,
    val isSaving: Boolean = false,
    val nameBlankError: Boolean = false,
    val submitError: String? = null,
)

sealed class AddClientEvent {
    data class NameChanged(val name: String) : AddClientEvent()
    data class PhoneChanged(val phone: String) : AddClientEvent()
    data class InitialCreditChanged(val input: String) : AddClientEvent()
    data class WalletSelected(val walletId: String?) : AddClientEvent()
    data object Save : AddClientEvent()
}

sealed class AddClientUiEvent {
    data object Saved : AddClientUiEvent()
}
