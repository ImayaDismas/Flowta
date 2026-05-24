package com.flowgroup.flowta.domain.usecase.reconciliation

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.PaymentDirection
import com.flowgroup.flowta.domain.model.ReceivedPayment
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.repository.ReconciliationRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.hours
import javax.inject.Inject

/**
 * Suggests the transaction most likely to correspond to a payment: a SALE for an inbound payment,
 * an EXPENSE for an outbound one.
 *
 * Signal: an exact amount match (same minor units and currency). Among exact-amount candidates of
 * the right type that are not already matched to another payment, the one closest in time within
 * [MATCH_WINDOW] wins. Returns null when nothing qualifies — the UI then offers "record as new".
 */
class SuggestMatchUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val reconciliationRepository: ReconciliationRepository,
) {
    suspend operator fun invoke(payment: ReceivedPayment): Result<Transaction?> {
        val wantType = when (payment.direction) {
            PaymentDirection.IN -> TransactionType.SALE
            PaymentDirection.OUT -> TransactionType.EXPENSE
        }
        val candidates = when (
            val r = transactionRepository.observeHistoryForBusiness(payment.businessId).first()
        ) {
            is Result.Success -> r.data.map { it.transaction }.filter { it.type == wantType }
            is Result.Error -> return r
        }
        val alreadyMatched = when (
            val r = reconciliationRepository.observeForBusiness(payment.businessId).first()
        ) {
            is Result.Success -> r.data.mapNotNull { it.matchedTransactionId }.toSet()
            is Result.Error -> return r
        }

        val best = candidates
            .filter {
                it.id !in alreadyMatched &&
                    it.amount.currency == payment.amount.currency &&
                    it.amount.minorUnits == payment.amount.minorUnits
            }
            .minByOrNull { (it.occurredAt - payment.occurredAt).absoluteValue }
            ?.takeIf { (it.occurredAt - payment.occurredAt).absoluteValue <= MATCH_WINDOW }

        return Result.Success(best)
    }

    private companion object {
        val MATCH_WINDOW = 48.hours
    }
}
