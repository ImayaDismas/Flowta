package com.flowgroup.flowta.domain.model

data class WalletDetail(
    val wallet: Wallet,
    val currentBalanceMinor: Long,
    val recentTransactions: List<TransactionWithWallet>,
    val weekTotals: TransactionTotals,
)
