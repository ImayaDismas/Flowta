package com.flowgroup.flowta.data.datasource.local

import com.flowgroup.flowta.data.datasource.local.dao.WalletDao
import com.flowgroup.flowta.data.model.entity.WalletEntity
import com.flowgroup.flowta.data.model.entity.projection.WalletWithBalanceProjection
import com.flowgroup.flowta.di.qualifier.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletLocalDataSource @Inject constructor(
    private val dao: WalletDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    fun observeForBusiness(businessId: String): Flow<List<WalletEntity>> =
        dao.observeForBusiness(businessId).flowOn(ioDispatcher)

    fun observeWithBalanceForBusiness(businessId: String): Flow<List<WalletWithBalanceProjection>> =
        dao.observeWithBalanceForBusiness(businessId).flowOn(ioDispatcher)

    suspend fun getById(id: String): WalletEntity? =
        withContext(ioDispatcher) { dao.getById(id) }

    suspend fun upsert(entity: WalletEntity) =
        withContext(ioDispatcher) { dao.upsert(entity) }

    suspend fun deleteById(id: String) =
        withContext(ioDispatcher) { dao.deleteById(id) }
}
