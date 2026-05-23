package com.flowgroup.flowta.ui.viewmodel.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.transaction.DeleteTransactionUseCase
import com.flowgroup.flowta.domain.usecase.transaction.ObserveTransactionDetailUseCase
import com.flowgroup.flowta.ui.state.transaction.TransactionDetailEvent
import com.flowgroup.flowta.ui.state.transaction.TransactionDetailUiEvent
import com.flowgroup.flowta.ui.state.transaction.TransactionDetailUiState
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
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeTransactionDetail: ObserveTransactionDetailUseCase,
    private val deleteTransaction: DeleteTransactionUseCase,
) : ViewModel() {

    val transactionId: String = checkNotNull(savedStateHandle.get<String>("transactionId"))

    private val _uiState = MutableStateFlow<TransactionDetailUiState>(TransactionDetailUiState.Loading)
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<TransactionDetailUiEvent>(Channel.BUFFERED)
    val events: Flow<TransactionDetailUiEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeTransactionDetail(transactionId).collect { result ->
                _uiState.update { current ->
                    when (result) {
                        is Result.Success -> result.data?.let { detail ->
                            (current as? TransactionDetailUiState.Content)?.copy(detail = detail)
                                ?: TransactionDetailUiState.Content(detail)
                        } ?: TransactionDetailUiState.NotFound
                        is Result.Error -> TransactionDetailUiState.Error(result.exception.message.orEmpty())
                    }
                }
            }
        }
    }

    fun onEvent(event: TransactionDetailEvent) {
        when (event) {
            TransactionDetailEvent.DeleteRequested -> updateContent { it.copy(confirmingDelete = true) }
            TransactionDetailEvent.DeleteDismissed -> updateContent { it.copy(confirmingDelete = false) }
            TransactionDetailEvent.DeleteConfirmed -> confirmDelete()
        }
    }

    private fun confirmDelete() {
        val current = _uiState.value as? TransactionDetailUiState.Content ?: return
        if (current.isDeleting) return
        updateContent { it.copy(confirmingDelete = false, isDeleting = true) }
        viewModelScope.launch {
            when (deleteTransaction(transactionId)) {
                is Result.Success -> _events.send(TransactionDetailUiEvent.Deleted)
                is Result.Error -> updateContent { it.copy(isDeleting = false) }
            }
        }
    }

    private inline fun updateContent(
        block: (TransactionDetailUiState.Content) -> TransactionDetailUiState.Content,
    ) {
        _uiState.update { state ->
            when (state) {
                is TransactionDetailUiState.Content -> block(state)
                else -> state
            }
        }
    }
}
