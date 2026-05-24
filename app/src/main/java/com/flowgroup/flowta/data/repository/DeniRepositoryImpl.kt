package com.flowgroup.flowta.data.repository

import com.flowgroup.flowta.data.datasource.local.ClientLocalDataSource
import com.flowgroup.flowta.data.datasource.local.DeniEntryLocalDataSource
import com.flowgroup.flowta.data.model.entity.DeniEntryEntity
import com.flowgroup.flowta.data.model.entity.projection.ClientWithBalanceProjection
import com.flowgroup.flowta.data.model.mapper.toDomain
import com.flowgroup.flowta.data.model.mapper.toEntity
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.common.resultOf
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Client
import com.flowgroup.flowta.domain.model.ClientDeni
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
    private val customerLocal: ClientLocalDataSource,
    private val deniEntryLocal: DeniEntryLocalDataSource,
    private val clock: Clock,
) : DeniRepository {

    override fun observeClientsWithBalance(businessId: String): Flow<Result<List<ClientDeni>>> =
        customerLocal.observeWithBalanceForBusiness(businessId)
            .map<List<ClientWithBalanceProjection>, Result<List<ClientDeni>>> { rows ->
                Result.Success(rows.map { it.toDomain() })
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override fun observeTotalOutstanding(businessId: String): Flow<Result<Long>> =
        deniEntryLocal.observeTotalOutstandingForBusiness(businessId)
            .map<Long, Result<Long>> { Result.Success(it) }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override fun observeClientWithBalance(clientId: String): Flow<Result<ClientDeni?>> =
        customerLocal.observeWithBalanceById(clientId)
            .map<ClientWithBalanceProjection?, Result<ClientDeni?>> { row ->
                Result.Success(row?.toDomain())
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override fun observeEntriesForClient(clientId: String): Flow<Result<List<DeniEntry>>> =
        deniEntryLocal.observeForClient(clientId)
            .map<List<DeniEntryEntity>, Result<List<DeniEntry>>> { rows ->
                Result.Success(rows.map { it.toDomain() })
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override suspend fun getClient(clientId: String): Result<Client?> = resultOf {
        customerLocal.getById(clientId)?.toDomain()
    }

    override suspend fun addClient(
        businessId: String,
        name: String,
        phone: String?,
        currency: CurrencyCode,
    ): Result<Client> = resultOf {
        val now = clock.now()
        val client = Client(
            id = UUID.randomUUID().toString(),
            businessId = businessId,
            name = name.trim(),
            phone = phone?.trim()?.takeIf { it.isNotEmpty() },
            currency = currency,
            createdAt = now,
            updatedAt = now,
        )
        customerLocal.upsert(client.toEntity())
        client
    }

    override suspend fun recordEntry(
        businessId: String,
        clientId: String,
        type: DeniEntryType,
        amount: Money,
        note: String?,
        walletId: String?,
    ): Result<DeniEntry> = resultOf {
        val now = clock.now()
        val entry = DeniEntry(
            id = UUID.randomUUID().toString(),
            businessId = businessId,
            clientId = clientId,
            type = type,
            amount = amount,
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            walletId = walletId,
            occurredAt = now,
            createdAt = now,
            updatedAt = now,
        )
        deniEntryLocal.upsert(entry.toEntity())
        entry
    }
}
