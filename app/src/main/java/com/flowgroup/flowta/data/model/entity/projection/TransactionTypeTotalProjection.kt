package com.flowgroup.flowta.data.model.entity.projection

import androidx.room.ColumnInfo
import com.flowgroup.flowta.domain.model.TransactionType

data class TransactionTypeTotalProjection(
    @ColumnInfo(name = "type") val type: TransactionType,
    @ColumnInfo(name = "total_minor") val totalMinor: Long,
)
