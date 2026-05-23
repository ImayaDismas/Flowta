package com.flowgroup.flowta.ui.state.deni

import com.flowgroup.flowta.domain.model.ClientDeniDetail

sealed class ClientDeniDetailUiState {
    data object Loading : ClientDeniDetailUiState()
    data object NotFound : ClientDeniDetailUiState()
    data class Content(
        val detail: ClientDeniDetail,
        val dialog: Dialog? = null,
        val amountInput: String = "",
        val noteInput: String = "",
        val amountError: Boolean = false,
        val isSubmitting: Boolean = false,
        val submitError: String? = null,
    ) : ClientDeniDetailUiState() {
        enum class Dialog { CREDIT, PAYMENT }
    }
    data class Error(val message: String) : ClientDeniDetailUiState()
}

sealed class ClientDeniDetailEvent {
    data object AddCreditClicked : ClientDeniDetailEvent()
    data object RecordPaymentClicked : ClientDeniDetailEvent()
    data object DialogDismissed : ClientDeniDetailEvent()
    data class AmountChanged(val input: String) : ClientDeniDetailEvent()
    data class NoteChanged(val note: String) : ClientDeniDetailEvent()
    data object DialogConfirmed : ClientDeniDetailEvent()
}
