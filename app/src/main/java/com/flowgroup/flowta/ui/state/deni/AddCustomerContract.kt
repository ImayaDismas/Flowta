package com.flowgroup.flowta.ui.state.deni

data class AddCustomerUiState(
    val name: String = "",
    val phone: String = "",
    val initialCreditInput: String = "",
    val isSaving: Boolean = false,
    val nameBlankError: Boolean = false,
    val submitError: String? = null,
)

sealed class AddCustomerEvent {
    data class NameChanged(val name: String) : AddCustomerEvent()
    data class PhoneChanged(val phone: String) : AddCustomerEvent()
    data class InitialCreditChanged(val input: String) : AddCustomerEvent()
    data object Save : AddCustomerEvent()
}

sealed class AddCustomerUiEvent {
    data object Saved : AddCustomerUiEvent()
}
