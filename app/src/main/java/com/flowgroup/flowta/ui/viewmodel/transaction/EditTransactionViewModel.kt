package com.flowgroup.flowta.ui.viewmodel.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.usecase.transaction.GetTransactionUseCase
import com.flowgroup.flowta.domain.usecase.transaction.UpdateTransactionUseCase
import com.flowgroup.flowta.domain.usecase.wallet.ObserveWalletsForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.transaction.EditTransactionEvent
import com.flowgroup.flowta.ui.state.transaction.EditTransactionUiEvent
import com.flowgroup.flowta.ui.state.transaction.EditTransactionUiState
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
class EditTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeWallets: ObserveWalletsForCurrentBusinessUseCase,
    private val getTransaction: GetTransactionUseCase,
    private val updateTransaction: UpdateTransactionUseCase,
) : ViewModel() {

    private val transactionId: String = checkNotNull(savedStateHandle.get<String>("transactionId"))

    private val _uiState = MutableStateFlow<EditTransactionUiState>(EditTransactionUiState.Loading)
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    private val _events = Channel<EditTransactionUiEvent>(Channel.BUFFERED)
    val events: Flow<EditTransactionUiEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val transaction = (getTransaction(transactionId) as? Result.Success)?.data
            if (transaction == null) {
                _uiState.value = EditTransactionUiState.NotFound
                return@launch
            }
            observeWallets().collect { result ->
                if (result is Result.Success) {
                    val wallets = result.data
                    _uiState.update { current ->
                        when {
                            wallets.isEmpty() -> EditTransactionUiState.NotFound
                            current is EditTransactionUiState.Content -> {
                                val stillExists = wallets.any { it.id == current.selectedWalletId }
                                current.copy(
                                    wallets = wallets,
                                    selectedWalletId = if (stillExists) current.selectedWalletId
                                    else wallets.first().id,
                                )
                            }
                            else -> EditTransactionUiState.Content(
                                wallets = wallets,
                                selectedWalletId = if (wallets.any { it.id == transaction.walletId }) {
                                    transaction.walletId
                                } else {
                                    wallets.first().id
                                },
                                type = transaction.type,
                                amountInput = transaction.amount.minorUnits.toString(),
                                note = transaction.note.orEmpty(),
                            )
                        }
                    }
                }
            }
        }
    }

    fun onEvent(event: EditTransactionEvent) {
        when (event) {
            is EditTransactionEvent.TypeChanged -> updateContent { it.copy(type = event.type) }
            is EditTransactionEvent.WalletChanged -> updateContent {
                it.copy(selectedWalletId = event.walletId)
            }
            is EditTransactionEvent.AmountChanged -> updateContent {
                it.copy(
                    amountInput = event.input.filter { c -> c.isDigit() },
                    amountError = null,
                    submitError = null,
                )
            }
            is EditTransactionEvent.NoteChanged -> updateContent {
                it.copy(note = event.note.take(MAX_NOTE_LENGTH))
            }
            EditTransactionEvent.Save -> save()
        }
    }

    private fun save() {
        val current = currentContent() ?: return
        if (current.isSaving) return

        val amount = current.amountInput.toLongOrNull()
        val amountError = when {
            current.amountInput.isBlank() -> EditTransactionUiState.Content.AmountError.Required
            amount == null || amount <= 0L -> EditTransactionUiState.Content.AmountError.Invalid
            else -> null
        }
        if (amountError != null) {
            updateContent { it.copy(amountError = amountError) }
            return
        }

        val wallet = current.selectedWallet
        val money = Money(minorUnits = amount!!, currency = wallet.openingBalance.currency)
        updateContent { it.copy(isSaving = true, submitError = null) }
        viewModelScope.launch {
            val result = updateTransaction(
                id = transactionId,
                walletId = wallet.id,
                type = current.type,
                amount = money,
                note = current.note,
            )
            when (result) {
                is Result.Success -> {
                    updateContent { it.copy(isSaving = false) }
                    _events.send(EditTransactionUiEvent.Saved)
                }
                is Result.Error -> updateContent {
                    it.copy(isSaving = false, submitError = result.exception.message)
                }
            }
        }
    }

    private fun currentContent(): EditTransactionUiState.Content? =
        _uiState.value as? EditTransactionUiState.Content

    private inline fun updateContent(
        block: (EditTransactionUiState.Content) -> EditTransactionUiState.Content,
    ) {
        _uiState.update { state ->
            when (state) {
                is EditTransactionUiState.Content -> block(state)
                else -> state
            }
        }
    }

    private companion object { const val MAX_NOTE_LENGTH = 140 }
}
