package com.flowgroup.flowta.data.datasource.local.converter

import androidx.room.TypeConverter
import com.flowgroup.flowta.domain.model.CurrencyCode

class CurrencyCodeConverter {
    @TypeConverter
    fun toIso(code: CurrencyCode?): String? = code?.iso4217

    @TypeConverter
    fun fromIso(value: String?): CurrencyCode? = value?.let { CurrencyCode(it) }
}