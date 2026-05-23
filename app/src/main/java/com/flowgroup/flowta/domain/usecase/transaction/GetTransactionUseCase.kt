package com.flowgroup.flowta.domain.usecase.transaction

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.repository.TransactionRepository
import javax.inject.Inject

class GetTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(id: String): Result<Transaction?> =
        transactionRepository.getById(id)
}
