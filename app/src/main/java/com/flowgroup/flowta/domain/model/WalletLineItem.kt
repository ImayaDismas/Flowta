package com.flowgroup.flowta.domain.model

import kotlinx.datetime.Instant

sealed class WalletLineItem {
    abstract val occurredAt: Instant

    data class LedgerTransaction(val item: TransactionWithWallet) : WalletLineItem() {
        override val occurredAt: Instant get() = item.transaction.occurredAt
    }

    data class DeniMovement(
        val entry: DeniEntry,
        val clientName: String,
    ) : WalletLineItem() {
        override val occurredAt: Instant get() = entry.occurredAt
    }
}
