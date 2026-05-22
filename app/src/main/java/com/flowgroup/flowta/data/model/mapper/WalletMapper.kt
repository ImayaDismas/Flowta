package com.flowgroup.flowta.data.model.mapper

import com.flowgroup.flowta.data.model.entity.WalletEntity
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Wallet

fun WalletEntity.toDomain(): Wallet = Wallet(
    id = walletId,
    businessId = businessId,
    name = name,
    type = type,
    openingBalance = Money(minorUnits = openingBalanceMinor, currency = currencyCode),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Wallet.toEntity(): WalletEntity = WalletEntity(
    walletId = id,
    businessId = businessId,
    name = name,
    type = type,
    currencyCode = openingBalance.currency,
    openingBalanceMinor = openingBalance.minorUnits,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
