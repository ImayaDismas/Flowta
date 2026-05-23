package com.flowgroup.flowta.data.model.mapper

import com.flowgroup.flowta.data.model.entity.ClientEntity
import com.flowgroup.flowta.data.model.entity.DeniEntryEntity
import com.flowgroup.flowta.data.model.entity.projection.ClientWithBalanceProjection
import com.flowgroup.flowta.domain.model.Client
import com.flowgroup.flowta.domain.model.ClientDeni
import com.flowgroup.flowta.domain.model.DeniEntry
import com.flowgroup.flowta.domain.model.Money

fun ClientEntity.toDomain(): Client = Client(
    id = clientId,
    businessId = businessId,
    name = name,
    phone = phone,
    currency = currencyCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Client.toEntity(): ClientEntity = ClientEntity(
    clientId = id,
    businessId = businessId,
    name = name,
    phone = phone,
    currencyCode = currency,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ClientWithBalanceProjection.toDomain(): ClientDeni = ClientDeni(
    client = client.toDomain(),
    outstandingMinor = outstandingMinor,
)

fun DeniEntryEntity.toDomain(): DeniEntry = DeniEntry(
    id = deniEntryId,
    businessId = businessId,
    clientId = clientId,
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
    clientId = clientId,
    type = type,
    amountMinor = amount.minorUnits,
    currencyCode = amount.currency,
    note = note,
    occurredAt = occurredAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
