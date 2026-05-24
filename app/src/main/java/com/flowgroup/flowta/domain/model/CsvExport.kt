package com.flowgroup.flowta.domain.model

/** Result of a CSV export: the file [content] and how many data rows it holds. */
data class CsvExport(
    val content: String,
    val rowCount: Int,
)
