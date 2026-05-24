package com.flowgroup.flowta.domain.model

enum class ReconciliationStatus {
    /** Received but not yet linked to a recorded sale. */
    UNMATCHED,

    /** Linked to a recorded SALE transaction. */
    MATCHED,

    /** Reviewed and dismissed as not a sale (e.g. a personal transfer). Hidden from the queue. */
    IGNORED,
}
