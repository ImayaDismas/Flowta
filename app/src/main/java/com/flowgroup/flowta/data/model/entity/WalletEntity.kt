package com.flowgroup.flowta.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.WalletType
import kotlinx.datetime.Instant

@Entity(
    tableName = "wallets",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["business_id"],
            childColumns = ["business_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["business_id"], name = "index_wallets_business_id")],
)
data class WalletEntity(
    @PrimaryKey @ColumnInfo(name = "wallet_id") val walletId: String,
    @ColumnInfo(name = "business_id") val businessId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: WalletType,
    @ColumnInfo(name = "currency_code") val currencyCode: CurrencyCode,
    @ColumnInfo(name = "opening_balance_minor") val openingBalanceMinor: Long,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
