package com.flowgroup.flowta.ui.viewmodel.wallet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.repository.WalletRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
import com.flowgroup.flowta.domain.usecase.wallet.DeleteWalletUseCase
import com.flowgroup.flowta.domain.usecase.wallet.UpdateWalletUseCase
import com.flowgroup.flowta.ui.state.wallet.EditWalletEvent
import com.flowgroup.flowta.ui.state.wallet.EditWalletUiEvent
import com.flowgroup.flowta.ui.state.wallet.EditWalletUiState
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
class EditWalletViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val updateWallet: UpdateWalletUseCase,
    private val deleteWallet: DeleteWalletUseCase,
) : ViewModel() {

    private val walletId: String = checkNotNull(savedStateHandle.get<String>("walletId"))

    private val _uiState = MutableStateFlow<EditWalletUiState>(EditWalletUiState.Loading)
    val uiState: StateFlow<EditWalletUiState> = _uiState.asStateFlow()

    private val _events = Channel<EditWalletUiEvent>(Channel.BUFFERED)
    val events: Flow<EditWalletUiEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            when (val result = walletRepository.getById(walletId)) {
                is Result.Success -> {
                    val wallet = result.data
                    _uiState.value = if (wallet == null) {
                        EditWalletUiState.NotFound
                    } else {
                        EditWalletUiState.Content(
                            walletId = wallet.id,
                            currency = wallet.openingBalance.currency,
                            openingBalanceMinor = wallet.openingBalance.minorUnits,
                            name = wallet.name,
                            type = wallet.type,
                        )
                    }
                }
                is Result.Error -> _uiState.value = EditWalletUiState.NotFound
            }
        }
    }

    fun onEvent(event: EditWalletEvent) {
        when (event) {
            is EditWalletEvent.NameChanged -> updateContent {
                it.copy(name = event.name, nameError = null, submitError = null)
            }
            is EditWalletEvent.TypeChanged -> updateContent {
                it.copy(type = event.type, submitError = null)
            }
            EditWalletEvent.Save -> save()
            EditWalletEvent.DeleteRequested -> requestDelete()
            EditWalletEvent.DeleteConfirmed -> confirmDelete()
            EditWalletEvent.DismissDialog -> updateContent { it.copy(deleteDialog = null) }
        }
    }

    private fun save() {
        val current = currentContent() ?: return
        if (current.isSaving) return

        val trimmed = current.name.trim()
        val nameError = when {
            trimmed.isBlank() -> EditWalletUiState.Content.NameError.Blank
            trimmed.length > MAX_NAME_LENGTH -> EditWalletUiState.Content.NameError.TooLong
            else -> null
        }
        if (nameError != null) {
            updateContent { it.copy(nameError = nameError) }
            return
        }

        updateContent { it.copy(isSaving = true, submitError = null) }
        viewModelScope.launch {
            when (val result = updateWallet(id = current.walletId, name = trimmed, type = current.type)) {
                is Result.Success -> {
                    updateContent { it.copy(isSaving = false) }
                    _events.send(EditWalletUiEvent.Saved)
                }
                is Result.Error -> updateContent {
                    it.copy(isSaving = false, submitError = result.exception.message)
                }
            }
        }
    }

    private fun requestDelete() {
        val current = currentContent() ?: return
        if (current.isDeleting) return
        viewModelScope.launch {
            when (val countResult = transactionRepository.countForWallet(current.walletId)) {
                is Result.Success -> updateContent {
                    val dialog = if (countResult.data > 0) {
                        EditWalletUiState.Content.DeleteDialog.Blocked(countResult.data)
                    } else {
                        EditWalletUiState.Content.DeleteDialog.Confirm
                    }
                    it.copy(deleteDialog = dialog, submitError = null)
                }
                is Result.Error -> updateContent {
                    it.copy(submitError = countResult.exception.message)
                }
            }
        }
    }

    private fun confirmDelete() {
        val current = currentContent() ?: return
        if (current.isDeleting) return
        updateContent { it.copy(isDeleting = true, deleteDialog = null) }
        viewModelScope.launch {
            when (val outcome = deleteWallet(current.walletId)) {
                is DeleteWalletUseCase.Outcome.Deleted -> _events.send(EditWalletUiEvent.Deleted)
                is DeleteWalletUseCase.Outcome.Blocked -> updateContent {
                    it.copy(
                        isDeleting = false,
                        deleteDialog = EditWalletUiState.Content.DeleteDialog.Blocked(
                            outcome.transactionCount,
                        ),
                    )
                }
                is DeleteWalletUseCase.Outcome.Failed -> updateContent {
                    it.copy(isDeleting = false, submitError = outcome.message)
                }
            }
        }
    }

    private fun currentContent(): EditWalletUiState.Content? =
        _uiState.value as? EditWalletUiState.Content

    private inline fun updateContent(block: (EditWalletUiState.Content) -> EditWalletUiState.Content) {
        _uiState.update { state ->
            when (state) {
                is EditWalletUiState.Content -> block(state)
                else -> state
            }
        }
    }

    private companion object { const val MAX_NAME_LENGTH = 80 }
}
