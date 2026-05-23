package com.flowgroup.flowta.data.datasource.local

import com.flowgroup.flowta.data.datasource.local.dao.ClientDao
import com.flowgroup.flowta.data.model.entity.ClientEntity
import com.flowgroup.flowta.data.model.entity.projection.ClientWithBalanceProjection
import com.flowgroup.flowta.di.qualifier.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientLocalDataSource @Inject constructor(
    private val dao: ClientDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    fun observeWithBalanceForBusiness(businessId: String): Flow<List<ClientWithBalanceProjection>> =
        dao.observeWithBalanceForBusiness(businessId).flowOn(ioDispatcher)

    fun observeWithBalanceById(id: String): Flow<ClientWithBalanceProjection?> =
        dao.observeWithBalanceById(id).flowOn(ioDispatcher)

    suspend fun getById(id: String): ClientEntity? =
        withContext(ioDispatcher) { dao.getById(id) }

    suspend fun upsert(entity: ClientEntity) =
        withContext(ioDispatcher) { dao.upsert(entity) }

    suspend fun deleteById(id: String) =
        withContext(ioDispatcher) { dao.deleteById(id) }
}
