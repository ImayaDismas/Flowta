package com.flowgroup.flowta.data.datasource.local

import com.flowgroup.flowta.data.datasource.local.dao.TransactionDao
import com.flowgroup.flowta.data.model.entity.TransactionEntity
import com.flowgroup.flowta.data.model.entity.projection.TransactionTypeTotalProjection
import com.flowgroup.flowta.data.model.entity.projection.TransactionWithWalletProjection
import com.flowgroup.flowta.di.qualifier.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionLocalDataSource @Inject constructor(
    private val dao: TransactionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    fun observeHistoryForBusiness(businessId: String): Flow<List<TransactionWithWalletProjection>> =
        dao.observeHistoryForBusiness(businessId).flowOn(ioDispatcher)

    fun observeTotalsBetween(
        businessId: String,
        start: Instant,
        endExclusive: Instant,
    ): Flow<List<TransactionTypeTotalProjection>> =
        dao.observeTotalsBetween(
            businessId = businessId,
            startEpochMillis = start.toEpochMilliseconds(),
            endExclusiveEpochMillis = endExclusive.toEpochMilliseconds(),
        ).flowOn(ioDispatcher)

    fun observeRecentForWallet(
        walletId: String,
        limit: Int,
    ): Flow<List<TransactionWithWalletProjection>> =
        dao.observeRecentForWallet(walletId, limit).flowOn(ioDispatcher)

    fun observeByIdWithWallet(id: String): Flow<TransactionWithWalletProjection?> =
        dao.observeByIdWithWallet(id).flowOn(ioDispatcher)

    suspend fun getById(id: String): TransactionEntity? =
        withContext(ioDispatcher) { dao.getById(id) }

    fun observeCountForWallet(walletId: String): Flow<Int> =
        dao.observeCountForWallet(walletId).flowOn(ioDispatcher)

    suspend fun countForWallet(walletId: String): Int =
        withContext(ioDispatcher) { dao.countForWallet(walletId) }

    fun observeWalletTotalsBetween(
        walletId: String,
        start: Instant,
        endExclusive: Instant,
    ): Flow<List<TransactionTypeTotalProjection>> =
        dao.observeWalletTotalsBetween(
            walletId = walletId,
            startEpochMillis = start.toEpochMilliseconds(),
            endExclusiveEpochMillis = endExclusive.toEpochMilliseconds(),
        ).flowOn(ioDispatcher)

    suspend fun upsert(entity: TransactionEntity) =
        withContext(ioDispatcher) { dao.upsert(entity) }

    suspend fun deleteById(id: String) =
        withContext(ioDispatcher) { dao.deleteById(id) }
}
