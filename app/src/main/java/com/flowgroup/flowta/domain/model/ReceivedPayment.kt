package com.flowgroup.flowta.domain.model

import kotlinx.datetime.Instant

/**
 * A mobile-money payment recorded for reconciliation, persisted and optionally matched to a SALE.
 *
 * [matchedTransactionId] is a soft link (no hard FK), mirroring how deni entries reference a
 * wallet — the payment's lifecycle is independent of any single sale.
 */
data class ReceivedPayment(
    val id: String,
    val businessId: String,
    val provider: MobileMoneyProvider,
    val amount: Money,
    val reference: String,
    val senderName: String?,
    val senderPhone: String?,
    val status: ReconciliationStatus,
    val matchedTransactionId: String?,
    val source: PaymentSource,
    val occurredAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
)
