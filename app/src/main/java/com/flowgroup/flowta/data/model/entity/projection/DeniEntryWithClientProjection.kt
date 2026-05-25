package com.flowgroup.flowta.data.model.entity.projection

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.flowgroup.flowta.data.model.entity.DeniEntryEntity

data class DeniEntryWithClientProjection(
    @Embedded val entry: DeniEntryEntity,
    @ColumnInfo(name = "client_name") val clientName: String,
)
