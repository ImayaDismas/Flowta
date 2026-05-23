package com.flowgroup.flowta.domain.usecase.transaction

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.repository.TransactionRepository
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
        id: String,
        walletId: String,
        type: TransactionType,
        amount: Money,
        note: String?,
    ): Result<Unit> {
        if (amount.minorUnits <= 0L) {
            return Result.Error(AppException.LocalException("Amount must be greater than zero"))
        }
        return transactionRepository.update(id, walletId, type, amount, note)
    }
}
