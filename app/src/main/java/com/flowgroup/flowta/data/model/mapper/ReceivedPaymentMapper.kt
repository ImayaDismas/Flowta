package com.flowgroup.flowta.data.model.mapper

import com.flowgroup.flowta.data.model.entity.ReceivedPaymentEntity
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.ReceivedPayment

fun ReceivedPaymentEntity.toDomain(): ReceivedPayment = ReceivedPayment(
    id = receivedPaymentId,
    businessId = businessId,
    provider = provider,
    amount = Money(minorUnits = amountMinor, currency = currencyCode),
    reference = reference,
    senderName = senderName,
    senderPhone = senderPhone,
    status = status,
    matchedTransactionId = matchedTransactionId,
    source = source,
    occurredAt = occurredAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ReceivedPayment.toEntity(): ReceivedPaymentEntity = ReceivedPaymentEntity(
    receivedPaymentId = id,
    businessId = businessId,
    provider = provider,
    amountMinor = amount.minorUnits,
    currencyCode = amount.currency,
    reference = reference,
    senderName = senderName,
    senderPhone = senderPhone,
    status = status,
    matchedTransactionId = matchedTransactionId,
    source = source,
    occurredAt = occurredAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
