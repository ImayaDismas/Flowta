package com.flowgroup.flowta.domain.repository

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionTotals
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.TransactionWithWallet
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface TransactionRepository {
    fun observeHistoryForBusiness(businessId: String): Flow<Result<List<TransactionWithWallet>>>
    fun observeTotalsBetween(
        businessId: String,
        start: Instant,
        endExclusive: Instant,
    ): Flow<Result<TransactionTotals>>
    fun observeRecentForWallet(walletId: String, limit: Int): Flow<Result<List<TransactionWithWallet>>>
    fun observeCountForWallet(walletId: String): Flow<Result<Int>>
    suspend fun countForWallet(walletId: String): Result<Int>
    fun observeWalletTotalsBetween(
        walletId: String,
        start: Instant,
        endExclusive: Instant,
    ): Flow<Result<TransactionTotals>>
    suspend fun record(
        businessId: String,
        walletId: String,
        type: TransactionType,
        amount: Money,
        note: String?,
    ): Result<Transaction>
    suspend fun deleteById(id: String): Result<Unit>
}
