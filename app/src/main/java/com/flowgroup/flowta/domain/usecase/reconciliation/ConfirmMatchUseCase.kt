package com.flowgroup.flowta.domain.usecase.reconciliation

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.repository.ReconciliationRepository
import javax.inject.Inject

class ConfirmMatchUseCase @Inject constructor(
    private val reconciliationRepository: ReconciliationRepository,
) {
    suspend operator fun invoke(paymentId: String, transactionId: String): Result<Unit> =
        reconciliationRepository.matchToTransaction(paymentId, transactionId)
}
