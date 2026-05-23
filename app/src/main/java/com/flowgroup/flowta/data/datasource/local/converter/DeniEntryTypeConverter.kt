package com.flowgroup.flowta.data.datasource.local.converter

import androidx.room.TypeConverter
import com.flowgroup.flowta.domain.model.DeniEntryType

class DeniEntryTypeConverter {
    @TypeConverter
    fun toName(type: DeniEntryType?): String? = type?.name

    @TypeConverter
    fun fromName(value: String?): DeniEntryType? = value?.let { DeniEntryType.valueOf(it) }
}
