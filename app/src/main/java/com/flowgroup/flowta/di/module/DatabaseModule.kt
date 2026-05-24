package com.flowgroup.flowta.di.module

import android.content.Context
import androidx.room.Room
import com.flowgroup.flowta.data.datasource.local.FlowtaDatabase
import com.flowgroup.flowta.data.datasource.local.dao.BusinessDao
import com.flowgroup.flowta.data.datasource.local.dao.ClientDao
import com.flowgroup.flowta.data.datasource.local.dao.DeniEntryDao
import com.flowgroup.flowta.data.datasource.local.dao.ReceivedPaymentDao
import com.flowgroup.flowta.data.datasource.local.dao.TransactionDao
import com.flowgroup.flowta.data.datasource.local.dao.WalletDao
import com.flowgroup.flowta.data.datasource.local.security.DatabaseKeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFlowtaDatabase(
        @ApplicationContext context: Context,
        keyProvider: DatabaseKeyProvider,
    ): FlowtaDatabase {
        val passphrase = keyProvider.obtainPassphrase()
        val factory = SupportOpenHelperFactory(passphrase)
        return Room.databaseBuilder(context, FlowtaDatabase::class.java, FlowtaDatabase.DATABASE_NAME)
            .openHelperFactory(factory)
            .addMigrations(
                FlowtaDatabase.MIGRATION_1_2,
                FlowtaDatabase.MIGRATION_2_3,
                FlowtaDatabase.MIGRATION_3_4,
                FlowtaDatabase.MIGRATION_4_5,
                FlowtaDatabase.MIGRATION_5_6,
            )
            .build()
    }

    @Provides
    fun provideBusinessDao(database: FlowtaDatabase): BusinessDao = database.businessDao()

    @Provides
    fun provideWalletDao(database: FlowtaDatabase): WalletDao = database.walletDao()

    @Provides
    fun provideTransactionDao(database: FlowtaDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideClientDao(database: FlowtaDatabase): ClientDao = database.clientDao()

    @Provides
    fun provideDeniEntryDao(database: FlowtaDatabase): DeniEntryDao = database.deniEntryDao()

    @Provides
    fun provideReceivedPaymentDao(database: FlowtaDatabase): ReceivedPaymentDao =
        database.receivedPaymentDao()
}
