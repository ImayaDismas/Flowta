package com.flowgroup.flowta.domain.model

import kotlinx.datetime.Instant

data class Wallet(
    val id: String,
    val businessId: String,
    val name: String,
    val type: WalletType,
    val openingBalance: Money,
    val createdAt: Instant,
    val updatedAt: Instant,
)
