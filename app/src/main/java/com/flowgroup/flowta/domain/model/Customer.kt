package com.flowgroup.flowta.domain.model

import kotlinx.datetime.Instant

data class Customer(
    val id: String,
    val businessId: String,
    val name: String,
    val phone: String?,
    val currency: CurrencyCode,
    val createdAt: Instant,
    val updatedAt: Instant,
)
