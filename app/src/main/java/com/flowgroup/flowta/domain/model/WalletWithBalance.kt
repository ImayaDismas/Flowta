package com.flowgroup.flowta.domain.model

/**
 * A wallet paired with its computed current balance (opening + sales - expenses) in minor units.
 * The signed balance is exposed as a Long rather than [Money] because balances can be negative
 * when expenses exceed funds, whereas [Money] models non-negative transacted amounts.
 */
data class WalletWithBalance(
    val wallet: Wallet,
    val currentBalanceMinor: Long,
)
