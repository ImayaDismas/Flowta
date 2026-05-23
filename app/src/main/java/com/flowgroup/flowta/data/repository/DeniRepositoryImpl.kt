package com.flowgroup.flowta.data.repository

import com.flowgroup.flowta.data.datasource.local.CustomerLocalDataSource
import com.flowgroup.flowta.data.datasource.local.DeniEntryLocalDataSource
import com.flowgroup.flowta.data.model.entity.DeniEntryEntity
import com.flowgroup.flowta.data.model.entity.projection.CustomerWithBalanceProjection
import com.flowgroup.flowta.data.model.mapper.toDomain
import com.flowgroup.flowta.data.model.mapper.toEntity
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.common.resultOf
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Customer
import com.flowgroup.flowta.domain.model.CustomerDeni
import com.flowgroup.flowta.domain.model.DeniEntry
import com.flowgroup.flowta.domain.model.DeniEntryType
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.repository.DeniRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeniRepositoryImpl @Inject constructor(
    private val customerLocal: CustomerLocalDataSource,
    private val deniEntryLocal: DeniEntryLocalDataSource,
    private val clock: Clock,
) : DeniRepository {

    override fun observeCustomersWithBalance(businessId: String): Flow<Result<List<CustomerDeni>>> =
        customerLocal.observeWithBalanceForBusiness(businessId)
            .map<List<CustomerWithBalanceProjection>, Result<List<CustomerDeni>>> { rows ->
                Result.Success(rows.map { it.toDomain() })
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override fun observeTotalOutstanding(businessId: String): Flow<Result<Long>> =
        deniEntryLocal.observeTotalOutstandingForBusiness(businessId)
            .map<Long, Result<Long>> { Result.Success(it) }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override fun observeCustomerWithBalance(customerId: String): Flow<Result<CustomerDeni?>> =
        customerLocal.observeWithBalanceById(customerId)
            .map<CustomerWithBalanceProjection?, Result<CustomerDeni?>> { row ->
                Result.Success(row?.toDomain())
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override fun observeEntriesForCustomer(customerId: String): Flow<Result<List<DeniEntry>>> =
        deniEntryLocal.observeForCustomer(customerId)
            .map<List<DeniEntryEntity>, Result<List<DeniEntry>>> { rows ->
                Result.Success(rows.map { it.toDomain() })
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override suspend fun getCustomer(customerId: String): Result<Customer?> = resultOf {
        customerLocal.getById(customerId)?.toDomain()
    }

    override suspend fun addCustomer(
        businessId: String,
        name: String,
        phone: String?,
        currency: CurrencyCode,
    ): Result<Customer> = resultOf {
        val now = clock.now()
        val customer = Customer(
            id = UUID.randomUUID().toString(),
            businessId = businessId,
            name = name.trim(),
            phone = phone?.trim()?.takeIf { it.isNotEmpty() },
            currency = currency,
            createdAt = now,
            updatedAt = now,
        )
        customerLocal.upsert(customer.toEntity())
        customer
    }

    override suspend fun recordEntry(
        businessId: String,
        customerId: String,
        type: DeniEntryType,
        amount: Money,
        note: String?,
    ): Result<DeniEntry> = resultOf {
        val now = clock.now()
        val entry = DeniEntry(
            id = UUID.randomUUID().toString(),
            businessId = businessId,
            customerId = customerId,
            type = type,
            amount = amount,
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            occurredAt = now,
            createdAt = now,
            updatedAt = now,
        )
        deniEntryLocal.upsert(entry.toEntity())
        entry
    }
}
