package com.flowgroup.flowta.data.datasource.local.converter

import androidx.room.TypeConverter
import com.flowgroup.flowta.domain.model.ReconciliationStatus

class ReconciliationStatusConverter {
    @TypeConverter
    fun toName(status: ReconciliationStatus?): String? = status?.name

    @TypeConverter
    fun fromName(value: String?): ReconciliationStatus? = value?.let { ReconciliationStatus.valueOf(it) }
}
