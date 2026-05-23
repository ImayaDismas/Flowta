package com.flowgroup.flowta.ui.viewmodel.deni

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.deni.RecordDeniCreditUseCase
import com.flowgroup.flowta.domain.usecase.deni.RecordDeniPaymentUseCase
import com.flowgroup.flowta.domain.usecase.deni.ObserveCustomerDeniUseCase
import com.flowgroup.flowta.ui.state.deni.CustomerDeniDetailEvent
import com.flowgroup.flowta.ui.state.deni.CustomerDeniDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerDeniDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCustomerDeni: ObserveCustomerDeniUseCase,
    private val recordCredit: RecordDeniCreditUseCase,
    private val recordPayment: RecordDeniPaymentUseCase,
) : ViewModel() {

    val customerId: String = checkNotNull(savedStateHandle.get<String>("customerId"))

    private val _uiState = MutableStateFlow<CustomerDeniDetailUiState>(CustomerDeniDetailUiState.Loading)
    val uiState: StateFlow<CustomerDeniDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCustomerDeni(customerId).collect { result ->
                _uiState.update { current ->
                    when (result) {
                        is Result.Success -> result.data?.let { detail ->
                            (current as? CustomerDeniDetailUiState.Content)?.copy(detail = detail)
                                ?: CustomerDeniDetailUiState.Content(detail)
                        } ?: CustomerDeniDetailUiState.NotFound
                        is Result.Error -> CustomerDeniDetailUiState.Error(result.exception.message.orEmpty())
                    }
                }
            }
        }
    }

    fun onEvent(event: CustomerDeniDetailEvent) {
        when (event) {
            CustomerDeniDetailEvent.AddCreditClicked -> openDialog(CustomerDeniDetailUiState.Content.Dialog.CREDIT)
            CustomerDeniDetailEvent.RecordPaymentClicked -> openDialog(CustomerDeniDetailUiState.Content.Dialog.PAYMENT)
            CustomerDeniDetailEvent.DialogDismissed -> updateContent {
                it.copy(dialog = null, amountInput = "", noteInput = "", amountError = false, submitError = null)
            }
            is CustomerDeniDetailEvent.AmountChanged -> updateContent {
                it.copy(amountInput = event.input.filter { c -> c.isDigit() }, amountError = false, submitError = null)
            }
            is CustomerDeniDetailEvent.NoteChanged -> updateContent {
                it.copy(noteInput = event.note.take(MAX_NOTE_LENGTH))
            }
            CustomerDeniDetailEvent.DialogConfirmed -> confirm()
        }
    }

    private fun openDialog(dialog: CustomerDeniDetailUiState.Content.Dialog) = updateContent {
        it.copy(dialog = dialog, amountInput = "", noteInput = "", amountError = false, submitError = null)
    }

    private fun confirm() {
        val current = _uiState.value as? CustomerDeniDetailUiState.Content ?: return
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
                CustomerDeniDetailUiState.Content.Dialog.CREDIT -> recordCredit(customerId, amount, note)
                CustomerDeniDetailUiState.Content.Dialog.PAYMENT -> recordPayment(customerId, amount, note)
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
        block: (CustomerDeniDetailUiState.Content) -> CustomerDeniDetailUiState.Content,
    ) {
        _uiState.update { state ->
            when (state) {
                is CustomerDeniDetailUiState.Content -> block(state)
                else -> state
            }
        }
    }

    private companion object { const val MAX_NOTE_LENGTH = 140 }
}
