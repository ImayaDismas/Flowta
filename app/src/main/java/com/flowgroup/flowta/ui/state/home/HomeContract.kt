package com.flowgroup.flowta.ui.state.home

import com.flowgroup.flowta.domain.model.CurrencyCode

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Content(
        val businessName: String,
        val currency: CurrencyCode,
    ) : HomeUiState()
    data object MissingBusiness : HomeUiState()
}

enum class HomeTab { Wallets, History, Insights }
