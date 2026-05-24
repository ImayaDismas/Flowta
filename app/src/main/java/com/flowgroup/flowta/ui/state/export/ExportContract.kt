package com.flowgroup.flowta.ui.state.export

data class ExportUiState(
    val isPreparing: Boolean = false,
    /** Non-null when a CSV is ready to be written to a user-chosen file. Cleared after saving. */
    val content: String? = null,
    val rowCount: Int? = null,
    val savedOk: Boolean = false,
    /** True when there is nothing to export. */
    val empty: Boolean = false,
    val errorMessage: String? = null,
)
