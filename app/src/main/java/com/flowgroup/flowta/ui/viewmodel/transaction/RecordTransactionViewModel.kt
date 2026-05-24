package com.flowgroup.flowta.ui.viewmodel.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.usecase.deni.ObserveClientsDeniForCurrentBusinessUseCase
import com.flowgroup.flowta.domain.usecase.transaction.RecordSaleOnCreditUseCase
import com.flowgroup.flowta.domain.usecase.transaction.RecordTransactionUseCase
import com.flowgroup.flowta.domain.usecase.transaction.SaleCreditClient
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordTransactionViewModel @Inject constructor(
    observeWallets: ObserveWalletsForCurrentBusinessUseCase,
    observeClients: ObserveClientsDeniForCurrentBusinessUseCase,
    private val recordTransaction: RecordTransactionUseCase,
    private val recordSaleOnCredit: RecordSaleOnCreditUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordTransactionUiState>(RecordTransactionUiState.Loading)
    val uiState: StateFlow<RecordTransactionUiState> = _uiState.asStateFlow()

    private val _events = Channel<RecordTransactionUiEvent>(Channel.BUFFERED)
    val events: Flow<RecordTransactionUiEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(observeWallets(), observeClients()) { walletsResult, clientsResult ->
                val wallets = (walletsResult as? Result.Success)?.data
                val clients = (clientsResult as? Result.Success)?.data ?: emptyList()
                wallets to clients
            }.collect { (wallets, clients) ->
                if (wallets == null) return@collect
                _uiState.update { current ->
                    if (wallets.isEmpty()) {
                        RecordTransactionUiState.NoWallets
                    } else if (current is RecordTransactionUiState.Content) {
                        val stillExists = wallets.any { it.id == current.selectedWalletId }
                        current.copy(
                            wallets = wallets,
                            selectedWalletId = if (stillExists) current.selectedWalletId else wallets.first().id,
                            clients = clients,
                            selectedClientId = current.selectedClientId
                                ?.takeIf { id -> clients.any { it.client.id == id } },
                        )
                    } else {
                        RecordTransactionUiState.Content(
                            wallets = wallets,
                            selectedWalletId = wallets.first().id,
                            clients = clients,
                        )
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
            is RecordTransactionEvent.CreditToggled -> updateContent { c ->
                c.copy(
                    onCredit = event.onCredit,
                    creditAmountInput = if (event.onCredit && c.creditAmountInput.isBlank()) {
                        c.amountInput
                    } else {
                        c.creditAmountInput
                    },
                    addingNewClient = if (event.onCredit && c.clients.isEmpty()) true else c.addingNewClient,
                    creditError = null,
                    clientError = false,
                    submitError = null,
                )
            }
            is RecordTransactionEvent.CreditAmountChanged -> updateContent {
                it.copy(
                    creditAmountInput = event.input.filter { c -> c.isDigit() },
                    creditError = null,
                    submitError = null,
                )
            }
            is RecordTransactionEvent.ClientSelected -> updateContent {
                it.copy(selectedClientId = event.clientId, addingNewClient = false, clientError = false)
            }
            RecordTransactionEvent.NewClientSelected -> updateContent {
                it.copy(addingNewClient = true, selectedClientId = null, clientError = false)
            }
            is RecordTransactionEvent.NewClientNameChanged -> updateContent {
                it.copy(newClientName = event.name.take(MAX_NAME_LENGTH), clientError = false)
            }
            is RecordTransactionEvent.NewClientPhoneChanged -> updateContent {
                it.copy(newClientPhone = event.phone.take(MAX_PHONE_LENGTH))
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
        val total = amount!!

        if (current.type == TransactionType.SALE && current.onCredit) {
            submitCreditSale(current, total)
        } else {
            submitPlain(current, total)
        }
    }

    private fun submitPlain(current: RecordTransactionUiState.Content, total: Long) {
        val wallet = current.selectedWallet
        val money = Money(minorUnits = total, currency = wallet.openingBalance.currency)
        updateContent { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            when (val result = recordTransaction(wallet.id, current.type, money, current.note)) {
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

    private fun submitCreditSale(current: RecordTransactionUiState.Content, total: Long) {
        val credit = current.creditAmountInput.toLongOrNull()
        val creditError = when {
            current.creditAmountInput.isBlank() -> RecordTransactionUiState.Content.CreditError.Required
            credit == null || credit <= 0L -> RecordTransactionUiState.Content.CreditError.Invalid
            credit > total -> RecordTransactionUiState.Content.CreditError.Exceeds
            else -> null
        }
        val clientChosen = if (current.addingNewClient) {
            current.newClientName.isNotBlank()
        } else {
            current.selectedClientId != null
        }
        if (creditError != null || !clientChosen) {
            updateContent { it.copy(creditError = creditError, clientError = !clientChosen) }
            return
        }

        val client = if (current.addingNewClient) {
            SaleCreditClient.New(current.newClientName, current.newClientPhone.ifBlank { null })
        } else {
            SaleCreditClient.Existing(current.selectedClientId!!)
        }
        updateContent { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            val result = recordSaleOnCredit(
                walletId = current.selectedWallet.id,
                totalAmountMinor = total,
                creditAmountMinor = credit!!,
                note = current.note,
                client = client,
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

    private companion object {
        const val MAX_NOTE_LENGTH = 140
        const val MAX_NAME_LENGTH = 80
        const val MAX_PHONE_LENGTH = 20
    }
}
