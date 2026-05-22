package com.flowgroup.flowta.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.flowgroup.flowta.data.datasource.local.converter.CurrencyCodeConverter
import com.flowgroup.flowta.data.datasource.local.converter.InstantConverter
import com.flowgroup.flowta.data.datasource.local.converter.WalletTypeConverter
import com.flowgroup.flowta.data.datasource.local.dao.BusinessDao
import com.flowgroup.flowta.data.datasource.local.dao.WalletDao
import com.flowgroup.flowta.data.model.entity.BusinessEntity
import com.flowgroup.flowta.data.model.entity.WalletEntity

@Database(
    entities = [BusinessEntity::class, WalletEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(InstantConverter::class, CurrencyCodeConverter::class, WalletTypeConverter::class)
abstract class FlowtaDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao
    abstract fun walletDao(): WalletDao

    companion object {
        const val DATABASE_NAME = "flowta.db"

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `wallets` (" +
                        "`wallet_id` TEXT NOT NULL, " +
                        "`business_id` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`currency_code` TEXT NOT NULL, " +
                        "`opening_balance_minor` INTEGER NOT NULL, " +
                        "`created_at` INTEGER NOT NULL, " +
                        "`updated_at` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`wallet_id`), " +
                        "FOREIGN KEY(`business_id`) REFERENCES `businesses`(`business_id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_wallets_business_id` " +
                        "ON `wallets` (`business_id`)"
                )
            }
        }
    }
}
