package com.flowgroup.flowta.domain.reconciliation.statement

/**
 * Minimal RFC-4180-style CSV tokenizer: handles quoted fields, commas and newlines inside quotes,
 * and "" escapes. Returns rows of string fields, dropping fully blank rows.
 */
internal fun parseCsv(text: String): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    var row = mutableListOf<String>()
    val field = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < text.length) {
        val c = text[i]
        when {
            inQuotes -> when {
                c == '"' && i + 1 < text.length && text[i + 1] == '"' -> {
                    field.append('"'); i++
                }
                c == '"' -> inQuotes = false
                else -> field.append(c)
            }
            c == '"' -> inQuotes = true
            c == ',' -> { row.add(field.toString().trim()); field.setLength(0) }
            c == '\r' -> Unit
            c == '\n' -> {
                row.add(field.toString().trim()); field.setLength(0)
                rows.add(row); row = mutableListOf()
            }
            else -> field.append(c)
        }
        i++
    }
    row.add(field.toString().trim())
    rows.add(row)
    return rows.filter { cells -> cells.any { it.isNotBlank() } }
}
