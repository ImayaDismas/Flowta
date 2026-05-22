package com.flowgroup.flowta.data.repository

import com.flowgroup.flowta.data.datasource.local.BusinessLocalDataSource
import com.flowgroup.flowta.data.model.mapper.toDomain
import com.flowgroup.flowta.data.model.mapper.toEntity
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.common.resultOf
import com.flowgroup.flowta.domain.model.Business
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.repository.BusinessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusinessRepositoryImpl @Inject constructor(
    private val local: BusinessLocalDataSource,
    private val clock: Clock,
) : BusinessRepository {

    override fun observeAll(): Flow<Result<List<Business>>> = local.observeAll()
        .map<List<com.flowgroup.flowta.data.model.entity.BusinessEntity>, Result<List<Business>>> { entities ->
            Result.Success(entities.map { it.toDomain() })
        }
        .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override fun observeById(id: String): Flow<Result<Business?>> = local.observeById(id)
        .map<com.flowgroup.flowta.data.model.entity.BusinessEntity?, Result<Business?>> { entity ->
            Result.Success(entity?.toDomain())
        }
        .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override suspend fun getById(id: String): Result<Business?> = resultOf {
        local.getById(id)?.toDomain()
    }

    override suspend fun create(name: String, currency: CurrencyCode): Result<Business> = resultOf {
        val now = clock.now()
        val business = Business(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            currency = currency,
            createdAt = now,
            updatedAt = now,
        )
        local.upsert(business.toEntity())
        business
    }

    override suspend fun update(business: Business): Result<Unit> = resultOf {
        local.upsert(business.copy(updatedAt = clock.now()).toEntity())
    }

    override suspend fun deleteById(id: String): Result<Unit> = resultOf {
        local.deleteById(id)
    }
}
