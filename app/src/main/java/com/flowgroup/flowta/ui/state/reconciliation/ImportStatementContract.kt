package com.flowgroup.flowta.ui.state.reconciliation

data class ImportStatementUiState(
    val isImporting: Boolean = false,
    val fileName: String? = null,
    /** Non-null once an import finished: number of new payments stored (0 = all duplicates). */
    val storedCount: Int? = null,
    /** A user-facing error message when the file could not be imported. */
    val errorMessage: String? = null,
)
