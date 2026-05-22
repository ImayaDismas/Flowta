package com.flowgroup.flowta.data.datasource.local.converter

import androidx.room.TypeConverter
import com.flowgroup.flowta.domain.model.WalletType

class WalletTypeConverter {
    @TypeConverter
    fun toName(type: WalletType?): String? = type?.name

    @TypeConverter
    fun fromName(value: String?): WalletType? = value?.let { WalletType.valueOf(it) }
}
