package com.flowgroup.flowta.data.datasource.local.converter

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

class InstantConverter {
    @TypeConverter
    fun toEpochMilli(instant: Instant?): Long? = instant?.toEpochMilliseconds()

    @TypeConverter
    fun fromEpochMilli(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }
}
