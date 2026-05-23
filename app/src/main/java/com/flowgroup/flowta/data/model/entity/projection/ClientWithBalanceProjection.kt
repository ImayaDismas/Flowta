package com.flowgroup.flowta.data.model.entity.projection

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.flowgroup.flowta.data.model.entity.ClientEntity

data class ClientWithBalanceProjection(
    @Embedded val client: ClientEntity,
    @ColumnInfo(name = "outstanding_minor") val outstandingMinor: Long,
)
