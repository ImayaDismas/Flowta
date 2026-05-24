package com.flowgroup.flowta.domain.usecase.reconciliation

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.PaymentDirection
import com.flowgroup.flowta.domain.model.ReceivedPayment
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.repository.ReconciliationRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Returns all transactions eligible for a manual match against [payment]:
 * - SALE for an IN payment, EXPENSE for an OUT payment
 * - not already linked to another payment
 * - sorted newest-first so the most recent options appear at the top
 */
class GetMatchableTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val reconciliationRepository: ReconciliationRepository,
) {
    suspend operator fun invoke(payment: ReceivedPayment): Result<List<Transaction>> {
        val wantType = when (payment.direction) {
            PaymentDirection.IN -> TransactionType.SALE
            PaymentDirection.OUT -> TransactionType.EXPENSE
        }
        val all = when (val r = transactionRepository.observeHistoryForBusiness(payment.businessId).first()) {
            is Result.Success -> r.data.map { it.transaction }.filter { it.type == wantType }
            is Result.Error -> return r
        }
        val alreadyMatched = when (val r = reconciliationRepository.observeForBusiness(payment.businessId).first()) {
            is Result.Success -> r.data.mapNotNull { it.matchedTransactionId }.toSet()
            is Result.Error -> return r
        }
        return Result.Success(
            all.filter { it.id !in alreadyMatched }.sortedByDescending { it.occurredAt },
        )
    }
}
