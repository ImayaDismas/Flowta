package com.flowgroup.flowta.data.model.mapper

import com.flowgroup.flowta.data.model.entity.TransactionEntity
import com.flowgroup.flowta.data.model.entity.projection.TransactionWithWalletProjection
import com.flowgroup.flowta.data.model.entity.projection.WalletWithBalanceProjection
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionWithWallet
import com.flowgroup.flowta.domain.model.WalletWithBalance

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = transactionId,
    businessId = businessId,
    walletId = walletId,
    type = type,
    amount = Money(minorUnits = amountMinor, currency = currencyCode),
    note = note,
    occurredAt = occurredAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    transactionId = id,
    businessId = businessId,
    walletId = walletId,
    type = type,
    amountMinor = amount.minorUnits,
    currencyCode = amount.currency,
    note = note,
    occurredAt = occurredAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun TransactionWithWalletProjection.toDomain(): TransactionWithWallet = TransactionWithWallet(
    transaction = transaction.toDomain(),
    walletName = walletName,
    walletType = walletType,
)

fun WalletWithBalanceProjection.toDomain(): WalletWithBalance = WalletWithBalance(
    wallet = wallet.toDomain(),
    currentBalanceMinor = wallet.openingBalanceMinor + netMinor,
)
