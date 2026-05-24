package com.flowgroup.flowta.data.datasource.local.converter

import androidx.room.TypeConverter
import com.flowgroup.flowta.domain.model.PaymentDirection

class PaymentDirectionConverter {
    @TypeConverter
    fun toName(direction: PaymentDirection?): String? = direction?.name

    @TypeConverter
    fun fromName(value: String?): PaymentDirection? = value?.let { PaymentDirection.valueOf(it) }
}
