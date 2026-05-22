package com.flowgroup.flowta.domain.usecase.transaction

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class RecordTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(
        walletId: String,
        type: TransactionType,
        amount: Money,
        note: String?,
    ): Result<Transaction> {
        if (amount.minorUnits <= 0L) {
            return Result.Error(AppException.LocalException("Amount must be greater than zero"))
        }
        val businessId = preferencesRepository.currentBusinessId.firstOrNull()
            ?: return Result.Error(AppException.LocalException("No current business selected"))
        return transactionRepository.record(businessId, walletId, type, amount, note)
    }
}
