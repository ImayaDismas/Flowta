package com.flowgroup.flowta.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.flowgroup.flowta.domain.model.CurrencyCode
import kotlinx.datetime.Instant

@Entity(
    tableName = "customers",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["business_id"],
            childColumns = ["business_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["business_id"], name = "index_customers_business_id")],
)
data class CustomerEntity(
    @PrimaryKey @ColumnInfo(name = "customer_id") val customerId: String,
    @ColumnInfo(name = "business_id") val businessId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "phone") val phone: String?,
    @ColumnInfo(name = "currency_code") val currencyCode: CurrencyCode,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
