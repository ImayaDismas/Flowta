package com.flowgroup.flowta.ui.viewmodel.deni

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.deni.RecordDeniCreditUseCase
import com.flowgroup.flowta.domain.usecase.deni.RecordDeniPaymentUseCase
import com.flowgroup.flowta.domain.usecase.deni.ObserveClientDeniUseCase
import com.flowgroup.flowta.ui.state.deni.ClientDeniDetailEvent
import com.flowgroup.flowta.ui.state.deni.ClientDeniDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientDeniDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeClientDeni: ObserveClientDeniUseCase,
    private val recordCredit: RecordDeniCreditUseCase,
    private val recordPayment: RecordDeniPaymentUseCase,
) : ViewModel() {

    val clientId: String = checkNotNull(savedStateHandle.get<String>("clientId"))

    private val _uiState = MutableStateFlow<ClientDeniDetailUiState>(ClientDeniDetailUiState.Loading)
    val uiState: StateFlow<ClientDeniDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeClientDeni(clientId).collect { result ->
                _uiState.update { current ->
                    when (result) {
                        is Result.Success -> result.data?.let { detail ->
                            (current as? ClientDeniDetailUiState.Content)?.copy(detail = detail)
                                ?: ClientDeniDetailUiState.Content(detail)
                        } ?: ClientDeniDetailUiState.NotFound
                        is Result.Error -> ClientDeniDetailUiState.Error(result.exception.message.orEmpty())
                    }
                }
            }
        }
    }

    fun onEvent(event: ClientDeniDetailEvent) {
        when (event) {
            ClientDeniDetailEvent.AddCreditClicked -> openDialog(ClientDeniDetailUiState.Content.Dialog.CREDIT)
            ClientDeniDetailEvent.RecordPaymentClicked -> openDialog(ClientDeniDetailUiState.Content.Dialog.PAYMENT)
            ClientDeniDetailEvent.DialogDismissed -> updateContent {
                it.copy(dialog = null, amountInput = "", noteInput = "", amountError = false, submitError = null)
            }
            is ClientDeniDetailEvent.AmountChanged -> updateContent {
                it.copy(amountInput = event.input.filter { c -> c.isDigit() }, amountError = false, submitError = null)
            }
            is ClientDeniDetailEvent.NoteChanged -> updateContent {
                it.copy(noteInput = event.note.take(MAX_NOTE_LENGTH))
            }
            ClientDeniDetailEvent.DialogConfirmed -> confirm()
        }
    }

    private fun openDialog(dialog: ClientDeniDetailUiState.Content.Dialog) = updateContent {
        it.copy(dialog = dialog, amountInput = "", noteInput = "", amountError = false, submitError = null)
    }

    private fun confirm() {
        val current = _uiState.value as? ClientDeniDetailUiState.Content ?: return
        val dialog = current.dialog ?: return
        if (current.isSubmitting) return

        val amount = current.amountInput.toLongOrNull()
        if (current.amountInput.isBlank() || amount == null || amount <= 0L) {
            updateContent { it.copy(amountError = true) }
            return
        }

        updateContent { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            val note = current.noteInput
            val result = when (dialog) {
                ClientDeniDetailUiState.Content.Dialog.CREDIT -> recordCredit(clientId, amount, note)
                ClientDeniDetailUiState.Content.Dialog.PAYMENT -> recordPayment(clientId, amount, note)
            }
            when (result) {
                is Result.Success -> updateContent {
                    it.copy(
                        dialog = null,
                        amountInput = "",
                        noteInput = "",
                        amountError = false,
                        isSubmitting = false,
                        submitError = null,
                    )
                }
                is Result.Error -> updateContent {
                    it.copy(isSubmitting = false, submitError = result.exception.message)
                }
            }
        }
    }

    private inline fun updateContent(
        block: (ClientDeniDetailUiState.Content) -> ClientDeniDetailUiState.Content,
    ) {
        _uiState.update { state ->
            when (state) {
                is ClientDeniDetailUiState.Content -> block(state)
                else -> state
            }
        }
    }

    private companion object { const val MAX_NOTE_LENGTH = 140 }
}
