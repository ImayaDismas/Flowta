package com.flowgroup.flowta.data.datasource.local

import com.flowgroup.flowta.data.datasource.local.dao.ReceivedPaymentDao
import com.flowgroup.flowta.data.model.entity.ReceivedPaymentEntity
import com.flowgroup.flowta.di.qualifier.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceivedPaymentLocalDataSource @Inject constructor(
    private val dao: ReceivedPaymentDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    fun observeForBusiness(businessId: String): Flow<List<ReceivedPaymentEntity>> =
        dao.observeForBusiness(businessId).flowOn(ioDispatcher)

    suspend fun getById(id: String): ReceivedPaymentEntity? =
        withContext(ioDispatcher) { dao.getById(id) }

    /** Returns the number of rows actually inserted (duplicates are ignored). */
    suspend fun insertAllIgnoringDuplicates(entities: List<ReceivedPaymentEntity>): Int =
        withContext(ioDispatcher) {
            dao.insertAllIgnoringDuplicates(entities).count { it != -1L }
        }

    suspend fun update(entity: ReceivedPaymentEntity) =
        withContext(ioDispatcher) { dao.update(entity) }

    suspend fun deleteById(id: String) =
        withContext(ioDispatcher) { dao.deleteById(id) }
}
