package com.flowgroup.flowta.data.datasource.local

import com.flowgroup.flowta.data.datasource.local.dao.WalletDao
import com.flowgroup.flowta.data.model.entity.WalletEntity
import com.flowgroup.flowta.data.model.entity.projection.WalletWithBalanceProjection
import com.flowgroup.flowta.di.qualifier.IoDispatcher
import com.flowgroup.flowta.domain.model.WalletType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
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

    fun observeById(id: String): Flow<WalletEntity?> =
        dao.observeById(id).flowOn(ioDispatcher)

    fun observeWithBalanceById(id: String): Flow<WalletWithBalanceProjection?> =
        dao.observeWithBalanceById(id).flowOn(ioDispatcher)

    suspend fun getById(id: String): WalletEntity? =
        withContext(ioDispatcher) { dao.getById(id) }

    suspend fun upsert(entity: WalletEntity) =
        withContext(ioDispatcher) { dao.upsert(entity) }

    suspend fun updateNameAndType(id: String, name: String, type: WalletType, updatedAt: Instant) =
        withContext(ioDispatcher) {
            dao.updateNameAndType(
                id = id,
                name = name,
                type = type,
                updatedAtEpochMillis = updatedAt.toEpochMilliseconds(),
            )
        }

    suspend fun deleteById(id: String) =
        withContext(ioDispatcher) { dao.deleteById(id) }
}
