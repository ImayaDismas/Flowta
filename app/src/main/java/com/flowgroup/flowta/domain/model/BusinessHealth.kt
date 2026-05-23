package com.flowgroup.flowta.domain.model

/**
 * Snapshot of a business's money flow over one [HealthPeriod].
 *
 * Deltas are the percent change vs the prior period of the same length, expressed as a Double
 * (e.g. +12.5 for +12.5%). They are `null` when the prior period had a zero total, because
 * a delta against zero is not meaningful — the UI renders this as "—".
 */
data class BusinessHealth(
    val period: HealthPeriod,
    val revenue: Money,
    val expenses: Money,
    val revenueDeltaPercent: Double?,
    val expensesDeltaPercent: Double?,
)
