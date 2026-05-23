package com.flowgroup.flowta.domain.model

/**
 * Aggregated transaction totals over a time range, expressed in minor units.
 * The originating currency is carried separately by the caller because a totals query
 * is scoped to a single business (which fixes the currency).
 */
data class TransactionTotals(
    val salesMinor: Long,
    val expensesMinor: Long,
) {
    companion object {
        val ZERO: TransactionTotals = TransactionTotals(salesMinor = 0L, expensesMinor = 0L)
    }
}
