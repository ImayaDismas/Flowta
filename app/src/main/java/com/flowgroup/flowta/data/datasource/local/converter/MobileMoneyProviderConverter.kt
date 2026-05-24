package com.flowgroup.flowta.data.datasource.local.converter

import androidx.room.TypeConverter
import com.flowgroup.flowta.domain.model.MobileMoneyProvider

class MobileMoneyProviderConverter {
    @TypeConverter
    fun toName(provider: MobileMoneyProvider?): String? = provider?.name

    @TypeConverter
    fun fromName(value: String?): MobileMoneyProvider? = value?.let { MobileMoneyProvider.valueOf(it) }
}
