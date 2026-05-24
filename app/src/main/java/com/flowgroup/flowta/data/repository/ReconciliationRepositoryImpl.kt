package com.flowgroup.flowta.data.repository

import com.flowgroup.flowta.data.datasource.local.ReceivedPaymentLocalDataSource
import com.flowgroup.flowta.data.model.entity.ReceivedPaymentEntity
import com.flowgroup.flowta.data.model.mapper.toDomain
import com.flowgroup.flowta.data.model.mapper.toEntity
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.common.resultOf
import com.flowgroup.flowta.domain.model.ParsedPayment
import com.flowgroup.flowta.domain.model.PaymentSource
import com.flowgroup.flowta.domain.model.ReceivedPayment
import com.flowgroup.flowta.domain.model.ReconciliationStatus
import com.flowgroup.flowta.domain.repository.ReconciliationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReconciliationRepositoryImpl @Inject constructor(
    private val local: ReceivedPaymentLocalDataSource,
    private val clock: Clock,
) : ReconciliationRepository {

    override fun observeForBusiness(businessId: String): Flow<Result<List<ReceivedPayment>>> =
        local.observeForBusiness(businessId)
            .map<List<ReceivedPaymentEntity>, Result<List<ReceivedPayment>>> { rows ->
                Result.Success(rows.map { it.toDomain() })
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override suspend fun getById(id: String): Result<ReceivedPayment?> = resultOf {
        local.getById(id)?.toDomain()
    }

    override suspend fun storeParsed(
        businessId: String,
        parsed: List<ParsedPayment>,
        source: PaymentSource,
    ): Result<Int> = resultOf {
        val now = clock.now()
        val entities = parsed.map { p ->
            ReceivedPayment(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                provider = p.provider,
                amount = p.amount,
                reference = p.reference,
                senderName = p.senderName,
                senderPhone = p.senderPhone,
                direction = p.direction,
                status = ReconciliationStatus.UNMATCHED,
                matchedTransactionId = null,
                source = source,
                occurredAt = p.occurredAt ?: now,
                createdAt = now,
                updatedAt = now,
            ).toEntity()
        }
        local.insertAllIgnoringDuplicates(entities)
    }

    override suspend fun matchToTransaction(
        paymentId: String,
        transactionId: String,
    ): Result<Unit> = updatePayment(paymentId) { entity ->
        entity.copy(
            status = ReconciliationStatus.MATCHED,
            matchedTransactionId = transactionId,
            updatedAt = clock.now(),
        )
    }

    override suspend fun clearMatch(paymentId: String): Result<Unit> = updatePayment(paymentId) { entity ->
        entity.copy(
            status = ReconciliationStatus.UNMATCHED,
            matchedTransactionId = null,
            updatedAt = clock.now(),
        )
    }

    override suspend fun ignore(paymentId: String): Result<Unit> = updatePayment(paymentId) { entity ->
        entity.copy(
            status = ReconciliationStatus.IGNORED,
            matchedTransactionId = null,
            updatedAt = clock.now(),
        )
    }

    private suspend inline fun updatePayment(
        paymentId: String,
        transform: (ReceivedPaymentEntity) -> ReceivedPaymentEntity,
    ): Result<Unit> = resultOf {
        val existing = local.getById(paymentId)
            ?: throw AppException.LocalException("Payment not found")
        local.update(transform(existing))
    }
}
