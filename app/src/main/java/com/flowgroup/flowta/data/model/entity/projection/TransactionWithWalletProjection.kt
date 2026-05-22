package com.flowgroup.flowta.data.model.entity.projection

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.flowgroup.flowta.data.model.entity.TransactionEntity
import com.flowgroup.flowta.domain.model.WalletType

data class TransactionWithWalletProjection(
    @Embedded val transaction: TransactionEntity,
    @ColumnInfo(name = "wallet_name") val walletName: String,
    @ColumnInfo(name = "wallet_type") val walletType: WalletType,
)
