package com.flowgroup.flowta.data.model.mapper

import com.flowgroup.flowta.data.model.entity.CustomerEntity
import com.flowgroup.flowta.data.model.entity.DeniEntryEntity
import com.flowgroup.flowta.data.model.entity.projection.CustomerWithBalanceProjection
import com.flowgroup.flowta.domain.model.Customer
import com.flowgroup.flowta.domain.model.CustomerDeni
import com.flowgroup.flowta.domain.model.DeniEntry
import com.flowgroup.flowta.domain.model.Money

fun CustomerEntity.toDomain(): Customer = Customer(
    id = customerId,
    businessId = businessId,
    name = name,
    phone = phone,
    currency = currencyCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Customer.toEntity(): CustomerEntity = CustomerEntity(
    customerId = id,
    businessId = businessId,
    name = name,
    phone = phone,
    currencyCode = currency,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun CustomerWithBalanceProjection.toDomain(): CustomerDeni = CustomerDeni(
    customer = customer.toDomain(),
    outstandingMinor = outstandingMinor,
)

fun DeniEntryEntity.toDomain(): DeniEntry = DeniEntry(
    id = deniEntryId,
    businessId = businessId,
    customerId = customerId,
    type = type,
    amount = Money(minorUnits = amountMinor, currency = currencyCode),
    note = note,
    occurredAt = occurredAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun DeniEntry.toEntity(): DeniEntryEntity = DeniEntryEntity(
    deniEntryId = id,
    businessId = businessId,
    customerId = customerId,
    type = type,
    amountMinor = amount.minorUnits,
    currencyCode = amount.currency,
    note = note,
    occurredAt = occurredAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
