package com.flowgroup.flowta.domain.repository

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.ParsedPayment
import com.flowgroup.flowta.domain.model.PaymentSource
import com.flowgroup.flowta.domain.model.ReceivedPayment
import kotlinx.coroutines.flow.Flow

interface ReconciliationRepository {
    fun observeForBusiness(businessId: String): Flow<Result<List<ReceivedPayment>>>

    suspend fun getById(id: String): Result<ReceivedPayment?>

    /** Persists parsed payments, ignoring duplicates by (business, provider, reference). Returns rows stored. */
    suspend fun storeParsed(
        businessId: String,
        parsed: List<ParsedPayment>,
        source: PaymentSource,
    ): Result<Int>

    /** Links a payment to a SALE transaction and marks it MATCHED. */
    suspend fun matchToTransaction(paymentId: String, transactionId: String): Result<Unit>

    /** Removes any link and returns the payment to the UNMATCHED queue. */
    suspend fun clearMatch(paymentId: String): Result<Unit>

    /** Dismisses a payment as not a sale (IGNORED), hiding it from the queue. */
    suspend fun ignore(paymentId: String): Result<Unit>
}
