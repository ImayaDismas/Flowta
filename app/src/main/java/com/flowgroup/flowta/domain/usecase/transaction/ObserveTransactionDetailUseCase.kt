package com.flowgroup.flowta.domain.usecase.transaction

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.TransactionWithWallet
import com.flowgroup.flowta.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTransactionDetailUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(id: String): Flow<Result<TransactionWithWallet?>> =
        transactionRepository.observeByIdWithWallet(id)
}
