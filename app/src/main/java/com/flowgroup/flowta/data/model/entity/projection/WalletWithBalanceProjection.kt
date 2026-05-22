package com.flowgroup.flowta.data.model.entity.projection

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.flowgroup.flowta.data.model.entity.WalletEntity

data class WalletWithBalanceProjection(
    @Embedded val wallet: WalletEntity,
    @ColumnInfo(name = "net_minor") val netMinor: Long,
)
