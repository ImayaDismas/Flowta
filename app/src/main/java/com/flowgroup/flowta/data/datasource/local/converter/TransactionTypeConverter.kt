package com.flowgroup.flowta.data.datasource.local.converter

import androidx.room.TypeConverter
import com.flowgroup.flowta.domain.model.TransactionType

class TransactionTypeConverter {
    @TypeConverter
    fun toName(type: TransactionType?): String? = type?.name

    @TypeConverter
    fun fromName(value: String?): TransactionType? = value?.let { TransactionType.valueOf(it) }
}
