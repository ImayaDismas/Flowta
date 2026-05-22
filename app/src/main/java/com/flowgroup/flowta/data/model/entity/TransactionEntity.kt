package com.flowgroup.flowta.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.TransactionType
import kotlinx.datetime.Instant

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["business_id"],
            childColumns = ["business_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["wallet_id"],
            childColumns = ["wallet_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(
            value = ["business_id", "occurred_at"],
            name = "index_transactions_business_id_occurred_at",
        ),
        Index(value = ["wallet_id"], name = "index_transactions_wallet_id"),
    ],
)
data class TransactionEntity(
    @PrimaryKey @ColumnInfo(name = "transaction_id") val transactionId: String,
    @ColumnInfo(name = "business_id") val businessId: String,
    @ColumnInfo(name = "wallet_id") val walletId: String,
    @ColumnInfo(name = "type") val type: TransactionType,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
    @ColumnInfo(name = "currency_code") val currencyCode: CurrencyCode,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "occurred_at") val occurredAt: Instant,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
