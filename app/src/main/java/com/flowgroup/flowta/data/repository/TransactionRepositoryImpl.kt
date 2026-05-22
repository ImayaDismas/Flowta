package com.flowgroup.flowta.data.repository

import com.flowgroup.flowta.data.datasource.local.TransactionLocalDataSource
import com.flowgroup.flowta.data.model.entity.projection.TransactionWithWalletProjection
import com.flowgroup.flowta.data.model.mapper.toDomain
import com.flowgroup.flowta.data.model.mapper.toEntity
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.common.resultOf
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.TransactionWithWallet
import com.flowgroup.flowta.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val local: TransactionLocalDataSource,
    private val clock: Clock,
) : TransactionRepository {

    override fun observeHistoryForBusiness(businessId: String): Flow<Result<List<TransactionWithWallet>>> =
        local.observeHistoryForBusiness(businessId)
            .map<List<TransactionWithWalletProjection>, Result<List<TransactionWithWallet>>> { rows ->
                Result.Success(rows.map { it.toDomain() })
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override suspend fun record(
        businessId: String,
        walletId: String,
        type: TransactionType,
        amount: Money,
        note: String?,
    ): Result<Transaction> = resultOf {
        val now = clock.now()
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            businessId = businessId,
            walletId = walletId,
            type = type,
            amount = amount,
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            occurredAt = now,
            createdAt = now,
            updatedAt = now,
        )
        local.upsert(transaction.toEntity())
        transaction
    }

    override suspend fun deleteById(id: String): Result<Unit> = resultOf {
        local.deleteById(id)
    }
}
