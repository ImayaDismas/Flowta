package com.flowgroup.flowta.domain.model

data class WalletDetail(
    val wallet: Wallet,
    val currentBalanceMinor: Long,
    val recentLineItems: List<WalletLineItem>,
    val weekTotals: TransactionTotals,
)
