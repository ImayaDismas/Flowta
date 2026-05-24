package com.flowgroup.flowta.ui.viewmodel.reconciliation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.reconciliation.ObserveReconciliationSummaryForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.reconciliation.ReconciliationHubUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReconciliationHubViewModel @Inject constructor(
    observeSummary: ObserveReconciliationSummaryForCurrentBusinessUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReconciliationHubUiState>(ReconciliationHubUiState.Loading)
    val uiState: StateFlow<ReconciliationHubUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeSummary().collect { result ->
                _uiState.value = when (result) {
                    is Result.Error -> ReconciliationHubUiState.Error(result.exception.message.orEmpty())
                    is Result.Success ->
                        if (result.data.totalCount == 0) ReconciliationHubUiState.Empty
                        else ReconciliationHubUiState.Content(result.data)
                }
            }
        }
    }
}
