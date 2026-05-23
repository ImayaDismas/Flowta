package com.flowgroup.flowta.di.module

import com.flowgroup.flowta.data.repository.BusinessRepositoryImpl
import com.flowgroup.flowta.data.repository.DeniRepositoryImpl
import com.flowgroup.flowta.data.repository.PinRepositoryImpl
import com.flowgroup.flowta.data.repository.PreferencesRepositoryImpl
import com.flowgroup.flowta.data.repository.TransactionRepositoryImpl
import com.flowgroup.flowta.data.repository.WalletRepositoryImpl
import com.flowgroup.flowta.domain.repository.BusinessRepository
import com.flowgroup.flowta.domain.repository.DeniRepository
import com.flowgroup.flowta.domain.repository.PinRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
import com.flowgroup.flowta.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBusinessRepository(impl: BusinessRepositoryImpl): BusinessRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindPinRepository(impl: PinRepositoryImpl): PinRepository

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindDeniRepository(impl: DeniRepositoryImpl): DeniRepository
}