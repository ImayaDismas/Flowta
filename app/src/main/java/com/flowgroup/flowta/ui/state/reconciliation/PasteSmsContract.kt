package com.flowgroup.flowta.ui.state.reconciliation

data class PasteSmsUiState(
    val input: String = "",
    val isParsing: Boolean = false,
    /** Non-null once parsing finished: number of new payments stored (0 = all duplicates). */
    val storedCount: Int? = null,
    /** True when the message could not be read as a payment. */
    val failed: Boolean = false,
) {
    val canParse: Boolean get() = input.isNotBlank() && !isParsing
}
