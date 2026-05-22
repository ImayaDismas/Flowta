package com.flowgroup.flowta.domain.model

import kotlinx.datetime.Instant

data class Business(
    val id: String,
    val name: String,
    val currency: CurrencyCode,
    val createdAt: Instant,
    val updatedAt: Instant,
)
