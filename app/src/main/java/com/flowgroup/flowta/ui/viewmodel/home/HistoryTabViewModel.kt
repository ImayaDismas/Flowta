package com.flowgroup.flowta.ui.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.transaction.ObserveHistoryForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.home.HistoryTabUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryTabViewModel @Inject constructor(
    observeHistory: ObserveHistoryForCurrentBusinessUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryTabUiState>(HistoryTabUiState.Loading)
    val uiState: StateFlow<HistoryTabUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeHistory().collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> HistoryTabUiState.Content(result.data)
                    is Result.Error -> HistoryTabUiState.Error(result.exception.message.orEmpty())
                }
            }
        }
    }
}
