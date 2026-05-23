package com.flowgroup.flowta.data.model.entity.projection

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.flowgroup.flowta.data.model.entity.CustomerEntity

data class CustomerWithBalanceProjection(
    @Embedded val customer: CustomerEntity,
    @ColumnInfo(name = "outstanding_minor") val outstandingMinor: Long,
)
