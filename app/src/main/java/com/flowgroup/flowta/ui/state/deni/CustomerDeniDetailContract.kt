package com.flowgroup.flowta.ui.state.deni

import com.flowgroup.flowta.domain.model.CustomerDeniDetail

sealed class CustomerDeniDetailUiState {
    data object Loading : CustomerDeniDetailUiState()
    data object NotFound : CustomerDeniDetailUiState()
    data class Content(
        val detail: CustomerDeniDetail,
        val dialog: Dialog? = null,
        val amountInput: String = "",
        val noteInput: String = "",
        val amountError: Boolean = false,
        val isSubmitting: Boolean = false,
        val submitError: String? = null,
    ) : CustomerDeniDetailUiState() {
        enum class Dialog { CREDIT, PAYMENT }
    }
    data class Error(val message: String) : CustomerDeniDetailUiState()
}

sealed class CustomerDeniDetailEvent {
    data object AddCreditClicked : CustomerDeniDetailEvent()
    data object RecordPaymentClicked : CustomerDeniDetailEvent()
    data object DialogDismissed : CustomerDeniDetailEvent()
    data class AmountChanged(val input: String) : CustomerDeniDetailEvent()
    data class NoteChanged(val note: String) : CustomerDeniDetailEvent()
    data object DialogConfirmed : CustomerDeniDetailEvent()
}
