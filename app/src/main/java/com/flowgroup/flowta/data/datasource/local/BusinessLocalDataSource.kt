package com.flowgroup.flowta.data.datasource.local

import com.flowgroup.flowta.data.datasource.local.dao.BusinessDao
import com.flowgroup.flowta.data.model.entity.BusinessEntity
import com.flowgroup.flowta.di.qualifier.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusinessLocalDataSource @Inject constructor(
    private val dao: BusinessDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    fun observeAll(): Flow<List<BusinessEntity>> = dao.observeAll().flowOn(ioDispatcher)

    fun observeById(id: String): Flow<BusinessEntity?> = dao.observeById(id).flowOn(ioDispatcher)

    suspend fun getById(id: String): BusinessEntity? =
        withContext(ioDispatcher) { dao.getById(id) }

    suspend fun upsert(entity: BusinessEntity) =
        withContext(ioDispatcher) { dao.upsert(entity) }

    suspend fun deleteById(id: String) =
        withContext(ioDispatcher) { dao.deleteById(id) }
}