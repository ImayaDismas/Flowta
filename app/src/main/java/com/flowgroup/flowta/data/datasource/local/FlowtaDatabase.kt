package com.flowgroup.flowta.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.flowgroup.flowta.data.datasource.local.converter.CurrencyCodeConverter
import com.flowgroup.flowta.data.datasource.local.converter.InstantConverter
import com.flowgroup.flowta.data.datasource.local.dao.BusinessDao
import com.flowgroup.flowta.data.model.entity.BusinessEntity

@Database(
    entities = [BusinessEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(InstantConverter::class, CurrencyCodeConverter::class)
abstract class FlowtaDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao

    companion object {
        const val DATABASE_NAME = "flowta.db"
    }
}