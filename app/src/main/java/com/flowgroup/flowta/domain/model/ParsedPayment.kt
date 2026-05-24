package com.flowgroup.flowta.domain.model

import kotlinx.datetime.Instant

/**
 * A mobile-money payment extracted from a provider message (SMS, OCR, statement row).
 * Pure parser output — not yet persisted and not yet matched to a sale.
 *
 * [occurredAt] is best-effort: providers vary in how (and whether) they format a timestamp,
 * so it may be null. The persistence layer falls back to capture time when it is null.
 *
 * [direction] is the money flow relative to the wallet: [PaymentDirection.IN] for received money,
 * [PaymentDirection.OUT] for money sent/paid/withdrawn. For OUT, [senderName]/[senderPhone] hold
 * the counterparty (the recipient).
 */
data class ParsedPayment(
    val provider: MobileMoneyProvider,
    val amount: Money,
    val reference: String,
    val senderName: String?,
    val senderPhone: String?,
    val occurredAt: Instant?,
    val rawMessage: String,
    val direction: PaymentDirection = PaymentDirection.IN,
)
