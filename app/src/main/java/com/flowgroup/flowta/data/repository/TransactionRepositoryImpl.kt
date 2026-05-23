package com.flowgroup.flowta.data.repository

import com.flowgroup.flowta.data.datasource.local.TransactionLocalDataSource
import com.flowgroup.flowta.data.model.entity.projection.TransactionTypeTotalProjection
import com.flowgroup.flowta.data.model.entity.projection.TransactionWithWalletProjection
import com.flowgroup.flowta.data.model.mapper.toDomain
import com.flowgroup.flowta.data.model.mapper.toEntity
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.common.resultOf
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionTotals
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.TransactionWithWallet
import com.flowgroup.flowta.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
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

    override fun observeTotalsBetween(
        businessId: String,
        start: Instant,
        endExclusive: Instant,
    ): Flow<Result<TransactionTotals>> =
        local.observeTotalsBetween(businessId, start, endExclusive)
            .map<List<TransactionTypeTotalProjection>, Result<TransactionTotals>> { rows ->
                Result.Success(rows.toTotals())
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override fun observeRecentForWallet(
        walletId: String,
        limit: Int,
    ): Flow<Result<List<TransactionWithWallet>>> =
        local.observeRecentForWallet(walletId, limit)
            .map<List<com.flowgroup.flowta.data.model.entity.projection.TransactionWithWalletProjection>, Result<List<TransactionWithWallet>>> { rows ->
                Result.Success(rows.map { it.toDomain() })
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override fun observeByIdWithWallet(id: String): Flow<Result<TransactionWithWallet?>> =
        local.observeByIdWithWallet(id)
            .map<TransactionWithWalletProjection?, Result<TransactionWithWallet?>> { row ->
                Result.Success(row?.toDomain())
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override suspend fun getById(id: String): Result<Transaction?> = resultOf {
        local.getById(id)?.toDomain()
    }

    override fun observeCountForWallet(walletId: String): Flow<Result<Int>> =
        local.observeCountForWallet(walletId)
            .map<Int, Result<Int>> { Result.Success(it) }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override suspend fun countForWallet(walletId: String): Result<Int> = resultOf {
        local.countForWallet(walletId)
    }

    override fun observeWalletTotalsBetween(
        walletId: String,
        start: Instant,
        endExclusive: Instant,
    ): Flow<Result<TransactionTotals>> =
        local.observeWalletTotalsBetween(walletId, start, endExclusive)
            .map<List<TransactionTypeTotalProjection>, Result<TransactionTotals>> { rows ->
                Result.Success(rows.toTotals())
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    private fun List<TransactionTypeTotalProjection>.toTotals(): TransactionTotals {
        var sales = 0L
        var expenses = 0L
        forEach { row ->
            when (row.type) {
                TransactionType.SALE -> sales += row.totalMinor
                TransactionType.EXPENSE -> expenses += row.totalMinor
            }
        }
        return TransactionTotals(salesMinor = sales, expensesMinor = expenses)
    }

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

    override suspend fun update(
        id: String,
        walletId: String,
        type: TransactionType,
        amount: Money,
        note: String?,
    ): Result<Unit> {
        val existing = local.getById(id)
            ?: return Result.Error(AppException.LocalException("Transaction not found"))
        return resultOf {
            local.upsert(
                existing.copy(
                    walletId = walletId,
                    type = type,
                    amountMinor = amount.minorUnits,
                    currencyCode = amount.currency,
                    note = note?.trim()?.takeIf { it.isNotEmpty() },
                    updatedAt = clock.now(),
                )
            )
        }
    }

    override suspend fun deleteById(id: String): Result<Unit> = resultOf {
        local.deleteById(id)
    }
}
