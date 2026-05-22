package com.flowgroup.flowta.domain.model

import kotlinx.datetime.Instant

data class Transaction(
    val id: String,
    val businessId: String,
    val walletId: String,
    val type: TransactionType,
    val amount: Money,
    val note: String?,
    val occurredAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
)
