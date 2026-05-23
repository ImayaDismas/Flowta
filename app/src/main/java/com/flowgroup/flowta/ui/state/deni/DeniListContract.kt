package com.flowgroup.flowta.ui.state.deni

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.ClientDeni

sealed class DeniListUiState {
    data object Loading : DeniListUiState()
    data object Empty : DeniListUiState()
    data class Content(
        val clients: List<ClientDeni>,
        val totalOutstandingMinor: Long,
        val currency: CurrencyCode,
    ) : DeniListUiState()
    data class Error(val message: String) : DeniListUiState()
}
