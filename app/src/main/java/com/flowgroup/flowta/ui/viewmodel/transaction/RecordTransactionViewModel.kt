package com.flowgroup.flowta.ui.viewmodel.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.usecase.transaction.RecordTransactionUseCase
import com.flowgroup.flowta.domain.usecase.wallet.ObserveWalletsForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.transaction.RecordTransactionEvent
import com.flowgroup.flowta.ui.state.transaction.RecordTransactionUiEvent
import com.flowgroup.flowta.ui.state.transaction.RecordTransactionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordTransactionViewModel @Inject constructor(
    observeWallets: ObserveWalletsForCurrentBusinessUseCase,
    private val recordTransaction: RecordTransactionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordTransactionUiState>(RecordTransactionUiState.Loading)
    val uiState: StateFlow<RecordTransactionUiState> = _uiState.asStateFlow()

    private val _events = Channel<RecordTransactionUiEvent>(Channel.BUFFERED)
    val events: Flow<RecordTransactionUiEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeWallets().collect { result ->
                if (result is Result.Success) {
                    val wallets = result.data
                    _uiState.update { current ->
                        if (wallets.isEmpty()) {
                            RecordTransactionUiState.NoWallets
                        } else if (current is RecordTransactionUiState.Content) {
                            val stillExists = wallets.any { it.id == current.selectedWalletId }
                            current.copy(
                                wallets = wallets,
                                selectedWalletId = if (stillExists) current.selectedWalletId else wallets.first().id,
                            )
                        } else {
                            RecordTransactionUiState.Content(
                                wallets = wallets,
                                selectedWalletId = wallets.first().id,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onEvent(event: RecordTransactionEvent) {
        when (event) {
            is RecordTransactionEvent.TypeChanged -> updateContent { it.copy(type = event.type) }
            is RecordTransactionEvent.WalletChanged -> updateContent {
                it.copy(selectedWalletId = event.walletId)
            }
            is RecordTransactionEvent.AmountChanged -> updateContent {
                it.copy(
                    amountInput = event.input.filter { c -> c.isDigit() },
                    amountError = null,
                    submitError = null,
                )
            }
            is RecordTransactionEvent.NoteChanged -> updateContent {
                it.copy(note = event.note.take(MAX_NOTE_LENGTH))
            }
            RecordTransactionEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val current = currentContent() ?: return
        if (current.isSubmitting) return

        val amount = current.amountInput.toLongOrNull()
        val amountError = when {
            current.amountInput.isBlank() -> RecordTransactionUiState.Content.AmountError.Required
            amount == null || amount <= 0L -> RecordTransactionUiState.Content.AmountError.Invalid
            else -> null
        }
        if (amountError != null) {
            updateContent { it.copy(amountError = amountError) }
            return
        }

        val wallet = current.selectedWallet
        val money = Money(minorUnits = amount!!, currency = wallet.openingBalance.currency)
        updateContent { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            val result = recordTransaction(
                walletId = wallet.id,
                type = current.type,
                amount = money,
                note = current.note,
            )
            when (result) {
                is Result.Success -> {
                    updateContent { it.copy(isSubmitting = false) }
                    _events.send(RecordTransactionUiEvent.Recorded)
                }
                is Result.Error -> updateContent {
                    it.copy(isSubmitting = false, submitError = result.exception.message)
                }
            }
        }
    }

    private fun currentContent(): RecordTransactionUiState.Content? =
        _uiState.value as? RecordTransactionUiState.Content

    private inline fun updateContent(
        block: (RecordTransactionUiState.Content) -> RecordTransactionUiState.Content,
    ) {
        _uiState.update { state ->
            when (state) {
                is RecordTransactionUiState.Content -> block(state)
                else -> state
            }
        }
    }

    private companion object { const val MAX_NOTE_LENGTH = 140 }
}
