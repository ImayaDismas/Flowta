package com.flowgroup.flowta.ui.state.onboarding

import com.flowgroup.flowta.domain.model.CurrencyCode

sealed class AddBusinessUiState {
    data class Content(
        val name: String = "",
        val nameError: NameError? = null,
        val currency: CurrencyCode = CurrencyCode.KES,
        val isSubmitting: Boolean = false,
        val submitError: String? = null,
    ) : AddBusinessUiState() {

        enum class NameError(val messageRes: Int) {
            Blank(com.flowgroup.flowta.R.string.add_business_error_blank),
            TooLong(com.flowgroup.flowta.R.string.add_business_error_too_long),
        }
    }
}

sealed class AddBusinessEvent {
    data class NameChanged(val name: String) : AddBusinessEvent()
    data class CurrencyChanged(val currency: CurrencyCode) : AddBusinessEvent()
    data object Submit : AddBusinessEvent()
}

sealed class AddBusinessUiEvent {
    data object NavigateNext : AddBusinessUiEvent()
}