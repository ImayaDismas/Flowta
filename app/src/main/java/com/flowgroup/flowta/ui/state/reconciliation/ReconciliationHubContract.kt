package com.flowgroup.flowta.ui.state.reconciliation

import com.flowgroup.flowta.domain.model.ReconciliationSummary

sealed class ReconciliationHubUiState {
    data object Loading : ReconciliationHubUiState()
    data object Empty : ReconciliationHubUiState()
    data class Content(val summary: ReconciliationSummary) : ReconciliationHubUiState()
    data class Error(val message: String) : ReconciliationHubUiState()
}
