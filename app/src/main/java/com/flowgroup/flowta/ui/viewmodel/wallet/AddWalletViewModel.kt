package com.flowgroup.flowta.ui.viewmodel.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.usecase.business.ObserveCurrentBusinessUseCase
import com.flowgroup.flowta.domain.usecase.wallet.CreateWalletUseCase
import com.flowgroup.flowta.ui.state.wallet.AddWalletEvent
import com.flowgroup.flowta.ui.state.wallet.AddWalletUiEvent
import com.flowgroup.flowta.ui.state.wallet.AddWalletUiState
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
class AddWalletViewModel @Inject constructor(
    observeCurrentBusiness: ObserveCurrentBusinessUseCase,
    private val createWallet: CreateWalletUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddWalletUiState>(AddWalletUiState.Loading)
    val uiState: StateFlow<AddWalletUiState> = _uiState.asStateFlow()

    private val _events = Channel<AddWalletUiEvent>(Channel.BUFFERED)
    val events: Flow<AddWalletUiEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeCurrentBusiness().collect { result ->
                if (result is Result.Success) {
                    val business = result.data
                    if (business == null) {
                        _uiState.value = AddWalletUiState.MissingBusiness
                    } else if (_uiState.value !is AddWalletUiState.Content) {
                        _uiState.value = AddWalletUiState.Content(currency = business.currency)
                    }
                }
            }
        }
    }

    fun onEvent(event: AddWalletEvent) {
        when (event) {
            is AddWalletEvent.TypeChanged -> updateContent { it.copy(type = event.type) }
            is AddWalletEvent.NameChanged -> updateContent {
                it.copy(name = event.name, nameError = null, submitError = null)
            }
            is AddWalletEvent.OpeningBalanceChanged -> updateContent {
                it.copy(
                    openingBalanceInput = event.input.filter { c -> c.isDigit() },
                    balanceError = null,
                    submitError = null,
                )
            }
            AddWalletEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val current = currentContent() ?: return
        if (current.isSubmitting) return

        val trimmedName = current.name.trim()
        val nameError = when {
            trimmedName.isBlank() -> AddWalletUiState.Content.NameError.Blank
            trimmedName.length > MAX_NAME_LENGTH -> AddWalletUiState.Content.NameError.TooLong
            else -> null
        }
        val minorUnits = if (current.openingBalanceInput.isBlank()) 0L
        else current.openingBalanceInput.toLongOrNull()
        val balanceError = if (minorUnits == null || minorUnits < 0L)
            AddWalletUiState.Content.BalanceError.Invalid else null

        if (nameError != null || balanceError != null) {
            updateContent { it.copy(nameError = nameError, balanceError = balanceError) }
            return
        }

        val openingBalance = Money(minorUnits = minorUnits!!, currency = current.currency)
        updateContent { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            when (val result = createWallet(trimmedName, current.type, openingBalance)) {
                is Result.Success -> {
                    updateContent { it.copy(isSubmitting = false) }
                    _events.send(AddWalletUiEvent.Created)
                }
                is Result.Error -> updateContent {
                    it.copy(isSubmitting = false, submitError = result.exception.message)
                }
            }
        }
    }

    private fun currentContent(): AddWalletUiState.Content? =
        _uiState.value as? AddWalletUiState.Content

    private inline fun updateContent(block: (AddWalletUiState.Content) -> AddWalletUiState.Content) {
        _uiState.update { state ->
            when (state) {
                is AddWalletUiState.Content -> block(state)
                else -> state
            }
        }
    }

    private companion object { const val MAX_NAME_LENGTH = 80 }
}
