package com.flowgroup.flowta.data.datasource.local

import com.flowgroup.flowta.data.datasource.local.dao.TransactionDao
import com.flowgroup.flowta.data.model.entity.TransactionEntity
import com.flowgroup.flowta.data.model.entity.projection.TransactionWithWalletProjection
import com.flowgroup.flowta.di.qualifier.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionLocalDataSource @Inject constructor(
    private val dao: TransactionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    fun observeHistoryForBusiness(businessId: String): Flow<List<TransactionWithWalletProjection>> =
        dao.observeHistoryForBusiness(businessId).flowOn(ioDispatcher)

    suspend fun upsert(entity: TransactionEntity) =
        withContext(ioDispatcher) { dao.upsert(entity) }

    suspend fun deleteById(id: String) =
        withContext(ioDispatcher) { dao.deleteById(id) }
}
