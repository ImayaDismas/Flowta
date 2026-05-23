package com.flowgroup.flowta.domain.model

import kotlinx.datetime.Instant

data class DeniEntry(
    val id: String,
    val businessId: String,
    val customerId: String,
    val type: DeniEntryType,
    val amount: Money,
    val note: String?,
    val occurredAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
)
