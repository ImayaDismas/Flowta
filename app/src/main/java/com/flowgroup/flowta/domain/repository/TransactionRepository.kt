package com.flowgroup.flowta.domain.repository

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.TransactionWithWallet
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeHistoryForBusiness(businessId: String): Flow<Result<List<TransactionWithWallet>>>
    suspend fun record(
        businessId: String,
        walletId: String,
        type: TransactionType,
        amount: Money,
        note: String?,
    ): Result<Transaction>
    suspend fun deleteById(id: String): Result<Unit>
}
