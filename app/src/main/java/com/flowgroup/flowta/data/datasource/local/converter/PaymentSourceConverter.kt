package com.flowgroup.flowta.data.datasource.local.converter

import androidx.room.TypeConverter
import com.flowgroup.flowta.domain.model.PaymentSource

class PaymentSourceConverter {
    @TypeConverter
    fun toName(source: PaymentSource?): String? = source?.name

    @TypeConverter
    fun fromName(value: String?): PaymentSource? = value?.let { PaymentSource.valueOf(it) }
}
