package com.flowgroup.flowta.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.DeniEntryType
import kotlinx.datetime.Instant

@Entity(
    tableName = "deni_entries",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["business_id"],
            childColumns = ["business_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["customer_id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["business_id"], name = "index_deni_entries_business_id"),
        Index(value = ["customer_id"], name = "index_deni_entries_customer_id"),
    ],
)
data class DeniEntryEntity(
    @PrimaryKey @ColumnInfo(name = "deni_entry_id") val deniEntryId: String,
    @ColumnInfo(name = "business_id") val businessId: String,
    @ColumnInfo(name = "customer_id") val customerId: String,
    @ColumnInfo(name = "type") val type: DeniEntryType,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
    @ColumnInfo(name = "currency_code") val currencyCode: CurrencyCode,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "occurred_at") val occurredAt: Instant,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
