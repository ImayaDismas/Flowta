package com.flowgroup.flowta.data.datasource.local

import com.flowgroup.flowta.data.datasource.local.dao.CustomerDao
import com.flowgroup.flowta.data.model.entity.CustomerEntity
import com.flowgroup.flowta.data.model.entity.projection.CustomerWithBalanceProjection
import com.flowgroup.flowta.di.qualifier.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerLocalDataSource @Inject constructor(
    private val dao: CustomerDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    fun observeWithBalanceForBusiness(businessId: String): Flow<List<CustomerWithBalanceProjection>> =
        dao.observeWithBalanceForBusiness(businessId).flowOn(ioDispatcher)

    fun observeWithBalanceById(id: String): Flow<CustomerWithBalanceProjection?> =
        dao.observeWithBalanceById(id).flowOn(ioDispatcher)

    suspend fun getById(id: String): CustomerEntity? =
        withContext(ioDispatcher) { dao.getById(id) }

    suspend fun upsert(entity: CustomerEntity) =
        withContext(ioDispatcher) { dao.upsert(entity) }

    suspend fun deleteById(id: String) =
        withContext(ioDispatcher) { dao.deleteById(id) }
}
