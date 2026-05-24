package com.flowgroup.flowta.domain.usecase.reconciliation

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.ReceivedPayment
import com.flowgroup.flowta.domain.repository.ReconciliationRepository
import javax.inject.Inject

class GetReceivedPaymentUseCase @Inject constructor(
    private val reconciliationRepository: ReconciliationRepository,
) {
    suspend operator fun invoke(paymentId: String): Result<ReceivedPayment?> =
        reconciliationRepository.getById(paymentId)
}
