package com.flowgroup.flowta.ui.viewmodel.deni

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.deni.AddClientUseCase
import com.flowgroup.flowta.domain.usecase.wallet.ObserveWalletsWithBalanceForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.deni.AddClientEvent
import com.flowgroup.flowta.ui.state.deni.AddClientUiEvent
import com.flowgroup.flowta.ui.state.deni.AddClientUiState
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
class AddClientViewModel @Inject constructor(
    private val addClient: AddClientUseCase,
    observeWallets: ObserveWalletsWithBalanceForCurrentBusinessUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddClientUiState())
    val uiState: StateFlow<AddClientUiState> = _uiState.asStateFlow()

    private val _events = Channel<AddClientUiEvent>(Channel.BUFFERED)
    val events: Flow<AddClientUiEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeWallets().collect { result ->
                val wallets = (result as? Result.Success)?.data ?: emptyList()
                _uiState.update { state ->
                    val stillValid = state.selectedWalletId?.takeIf { id -> wallets.any { it.wallet.id == id } }
                    state.copy(wallets = wallets, selectedWalletId = stillValid)
                }
            }
        }
    }

    fun onEvent(event: AddClientEvent) {
        when (event) {
            is AddClientEvent.NameChanged -> _uiState.update {
                it.copy(name = event.name.take(MAX_NAME_LENGTH), nameBlankError = false, submitError = null)
            }
            is AddClientEvent.PhoneChanged -> _uiState.update {
                it.copy(phone = event.phone.take(MAX_PHONE_LENGTH))
            }
            is AddClientEvent.InitialCreditChanged -> _uiState.update {
                it.copy(initialCreditInput = event.input.filter { c -> c.isDigit() }, submitError = null)
            }
            is AddClientEvent.WalletSelected -> _uiState.update {
                it.copy(selectedWalletId = event.walletId)
            }
            AddClientEvent.Save -> save()
        }
    }

    private fun save() {
        val current = _uiState.value
        if (current.isSaving) return
        if (current.name.isBlank()) {
            _uiState.update { it.copy(nameBlankError = true) }
            return
        }
        val initialCredit = current.initialCreditInput.toLongOrNull() ?: 0L
        val walletId = current.selectedWalletId?.takeIf { initialCredit > 0L }
        _uiState.update { it.copy(isSaving = true, submitError = null) }
        viewModelScope.launch {
            when (val result = addClient(current.name, current.phone, initialCredit, walletId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(AddClientUiEvent.Saved)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isSaving = false, submitError = result.exception.message)
                }
            }
        }
    }

    private companion object {
        const val MAX_NAME_LENGTH = 80
        const val MAX_PHONE_LENGTH = 20
    }
}
