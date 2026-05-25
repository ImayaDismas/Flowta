package com.flowgroup.flowta.data.datasource.local

import com.flowgroup.flowta.data.datasource.local.dao.DeniEntryDao
import com.flowgroup.flowta.data.model.entity.DeniEntryEntity
import com.flowgroup.flowta.data.model.entity.projection.DeniEntryWithClientProjection
import com.flowgroup.flowta.di.qualifier.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeniEntryLocalDataSource @Inject constructor(
    private val dao: DeniEntryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    fun observeForClient(clientId: String): Flow<List<DeniEntryEntity>> =
        dao.observeForClient(clientId).flowOn(ioDispatcher)

    fun observeForWallet(walletId: String): Flow<List<DeniEntryWithClientProjection>> =
        dao.observeForWallet(walletId).flowOn(ioDispatcher)

    fun observeTotalOutstandingForBusiness(businessId: String): Flow<Long> =
        dao.observeTotalOutstandingForBusiness(businessId).flowOn(ioDispatcher)

    suspend fun upsert(entity: DeniEntryEntity) =
        withContext(ioDispatcher) { dao.upsert(entity) }

    suspend fun deleteById(id: String) =
        withContext(ioDispatcher) { dao.deleteById(id) }
}
