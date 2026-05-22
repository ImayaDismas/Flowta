package com.flowgroup.flowta.domain.model

data class TransactionWithWallet(
    val transaction: Transaction,
    val walletName: String,
    val walletType: WalletType,
)
