package com.flowgroup.flowta.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.flowgroup.flowta.domain.model.CurrencyCode
import kotlinx.datetime.Instant

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey @ColumnInfo(name = "business_id") val businessId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "currency_code") val currencyCode: CurrencyCode,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)