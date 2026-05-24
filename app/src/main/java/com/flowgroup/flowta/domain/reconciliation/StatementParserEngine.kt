package com.flowgroup.flowta.domain.reconciliation

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.ParsedPayment
import com.flowgroup.flowta.domain.reconciliation.statement.parseCsv
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses a statement file's CSV text into received payments. Scans for the first row a registered
 * [StatementParser] recognises as its header (statements often have preamble rows), then delegates.
 */
@Singleton
class StatementParserEngine @Inject constructor(
    private val parsers: Set<@JvmSuppressWildcards StatementParser>,
) {
    fun parse(csvText: String, currency: CurrencyCode): List<ParsedPayment> {
        val rows = parseCsv(csvText)
        rows.forEachIndexed { index, row ->
            val parser = parsers.firstOrNull { it.canParse(row) }
            if (parser != null) {
                return parser.parse(row, rows.drop(index + 1), currency)
            }
        }
        return emptyList()
    }
}
