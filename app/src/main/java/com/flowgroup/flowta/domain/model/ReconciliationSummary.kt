package com.flowgroup.flowta.domain.model

/**
 * Hub view of reconciliation: payments still needing review vs. those linked to a sale.
 * IGNORED payments are excluded from both lists.
 */
data class ReconciliationSummary(
    val unmatched: List<ReceivedPayment>,
    val matched: List<ReceivedPayment>,
) {
    val unmatchedCount: Int get() = unmatched.size
    val matchedCount: Int get() = matched.size
    val totalCount: Int get() = unmatchedCount + matchedCount
}
