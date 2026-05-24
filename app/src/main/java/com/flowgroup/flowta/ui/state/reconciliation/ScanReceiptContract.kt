package com.flowgroup.flowta.ui.state.reconciliation

data class ScanReceiptUiState(
    val isProcessing: Boolean = false,
    /** Non-null once a scan finished: number of new payments stored (0 = all duplicates). */
    val storedCount: Int? = null,
    /** True when the image could not be read as a payment. */
    val failed: Boolean = false,
)
