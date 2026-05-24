package com.flowgroup.flowta.domain.usecase.reconciliation

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.PaymentDirection
import com.flowgroup.flowta.domain.model.ReconciliationStatus
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.repository.ReconciliationRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * "Record as new": creates a transaction in [walletId] for an unmatched payment's amount, then
 * links the payment to it. An inbound payment becomes a SALE, an outbound one an EXPENSE — so the
 * wallet moves the right way. Used when the payment has no existing transaction to match against.
 */
class RecordTransactionFromPaymentUseCase @Inject constructor(
    private val reconciliationRepository: ReconciliationRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(paymentId: String, walletId: String): Result<Unit> {
        val payment = when (val r = reconciliationRepository.getById(paymentId)) {
            is Result.Success -> r.data
                ?: return Result.Error(AppException.LocalException("Payment not found"))
            is Result.Error -> return r
        }
        if (payment.status == ReconciliationStatus.MATCHED) {
            return Result.Error(AppException.LocalException("Payment is already matched"))
        }

        val type = when (payment.direction) {
            PaymentDirection.IN -> TransactionType.SALE
            PaymentDirection.OUT -> TransactionType.EXPENSE
        }

        val note = buildString {
            append(payment.provider.name)
            payment.senderName?.let { append(" • ").append(it) }
            append(" • Ref ").append(payment.reference)
        }

        val transaction = when (
            val r = transactionRepository.record(
                businessId = payment.businessId,
                walletId = walletId,
                type = type,
                amount = payment.amount,
                note = note,
            )
        ) {
            is Result.Success -> r.data
            is Result.Error -> return r
        }

        return reconciliationRepository.matchToTransaction(paymentId, transaction.id)
    }
}
