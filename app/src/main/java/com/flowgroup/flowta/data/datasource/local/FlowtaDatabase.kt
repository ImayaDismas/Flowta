package com.flowgroup.flowta.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.flowgroup.flowta.data.datasource.local.converter.CurrencyCodeConverter
import com.flowgroup.flowta.data.datasource.local.converter.DeniEntryTypeConverter
import com.flowgroup.flowta.data.datasource.local.converter.InstantConverter
import com.flowgroup.flowta.data.datasource.local.converter.TransactionTypeConverter
import com.flowgroup.flowta.data.datasource.local.converter.WalletTypeConverter
import com.flowgroup.flowta.data.datasource.local.dao.BusinessDao
import com.flowgroup.flowta.data.datasource.local.dao.ClientDao
import com.flowgroup.flowta.data.datasource.local.dao.DeniEntryDao
import com.flowgroup.flowta.data.datasource.local.dao.TransactionDao
import com.flowgroup.flowta.data.datasource.local.dao.WalletDao
import com.flowgroup.flowta.data.model.entity.BusinessEntity
import com.flowgroup.flowta.data.model.entity.ClientEntity
import com.flowgroup.flowta.data.model.entity.DeniEntryEntity
import com.flowgroup.flowta.data.model.entity.TransactionEntity
import com.flowgroup.flowta.data.model.entity.WalletEntity

@Database(
    entities = [
        BusinessEntity::class,
        WalletEntity::class,
        TransactionEntity::class,
        ClientEntity::class,
        DeniEntryEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
@TypeConverters(
    InstantConverter::class,
    CurrencyCodeConverter::class,
    WalletTypeConverter::class,
    TransactionTypeConverter::class,
    DeniEntryTypeConverter::class,
)
abstract class FlowtaDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun clientDao(): ClientDao
    abstract fun deniEntryDao(): DeniEntryDao

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

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `transactions` (" +
                        "`transaction_id` TEXT NOT NULL, " +
                        "`business_id` TEXT NOT NULL, " +
                        "`wallet_id` TEXT NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`amount_minor` INTEGER NOT NULL, " +
                        "`currency_code` TEXT NOT NULL, " +
                        "`note` TEXT, " +
                        "`occurred_at` INTEGER NOT NULL, " +
                        "`created_at` INTEGER NOT NULL, " +
                        "`updated_at` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`transaction_id`), " +
                        "FOREIGN KEY(`business_id`) REFERENCES `businesses`(`business_id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                        "FOREIGN KEY(`wallet_id`) REFERENCES `wallets`(`wallet_id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_transactions_business_id_occurred_at` " +
                        "ON `transactions` (`business_id`, `occurred_at`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_transactions_wallet_id` " +
                        "ON `transactions` (`wallet_id`)"
                )
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `customers` (" +
                        "`customer_id` TEXT NOT NULL, " +
                        "`business_id` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`phone` TEXT, " +
                        "`currency_code` TEXT NOT NULL, " +
                        "`created_at` INTEGER NOT NULL, " +
                        "`updated_at` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`customer_id`), " +
                        "FOREIGN KEY(`business_id`) REFERENCES `businesses`(`business_id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_customers_business_id` " +
                        "ON `customers` (`business_id`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `deni_entries` (" +
                        "`deni_entry_id` TEXT NOT NULL, " +
                        "`business_id` TEXT NOT NULL, " +
                        "`customer_id` TEXT NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`amount_minor` INTEGER NOT NULL, " +
                        "`currency_code` TEXT NOT NULL, " +
                        "`note` TEXT, " +
                        "`occurred_at` INTEGER NOT NULL, " +
                        "`created_at` INTEGER NOT NULL, " +
                        "`updated_at` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`deni_entry_id`), " +
                        "FOREIGN KEY(`business_id`) REFERENCES `businesses`(`business_id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                        "FOREIGN KEY(`customer_id`) REFERENCES `customers`(`customer_id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_deni_entries_business_id` " +
                        "ON `deni_entries` (`business_id`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_deni_entries_customer_id` " +
                        "ON `deni_entries` (`customer_id`)"
                )
            }
        }

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `deni_entries` ADD COLUMN `wallet_id` TEXT")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_deni_entries_wallet_id` " +
                        "ON `deni_entries` (`wallet_id`)"
                )
            }
        }
    }
}
