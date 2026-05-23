package com.flowgroup.flowta.data.repository

import com.flowgroup.flowta.data.datasource.local.WalletLocalDataSource
import com.flowgroup.flowta.data.model.entity.WalletEntity
import com.flowgroup.flowta.data.model.entity.projection.WalletWithBalanceProjection
import com.flowgroup.flowta.data.model.mapper.toDomain
import com.flowgroup.flowta.data.model.mapper.toEntity
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.common.resultOf
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Wallet
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.model.WalletWithBalance
import com.flowgroup.flowta.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val local: WalletLocalDataSource,
    private val clock: Clock,
) : WalletRepository {

    override fun observeForBusiness(businessId: String): Flow<Result<List<Wallet>>> =
        local.observeForBusiness(businessId)
            .map<List<WalletEntity>, Result<List<Wallet>>> { entities ->
                Result.Success(entities.map { it.toDomain() })
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override fun observeWithBalanceForBusiness(businessId: String): Flow<Result<List<WalletWithBalance>>> =
        local.observeWithBalanceForBusiness(businessId)
            .map<List<WalletWithBalanceProjection>, Result<List<WalletWithBalance>>> { rows ->
                Result.Success(rows.map { it.toDomain() })
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override fun observeById(id: String): Flow<Result<Wallet?>> =
        local.observeById(id)
            .map<WalletEntity?, Result<Wallet?>> { entity ->
                Result.Success(entity?.toDomain())
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override fun observeWithBalanceById(id: String): Flow<Result<WalletWithBalance?>> =
        local.observeWithBalanceById(id)
            .map<WalletWithBalanceProjection?, Result<WalletWithBalance?>> { projection ->
                Result.Success(projection?.toDomain())
            }
            .catch { e -> emit(Result.Error(AppException.LocalException(e.message.orEmpty()))) }

    override suspend fun getById(id: String): Result<Wallet?> = resultOf {
        local.getById(id)?.toDomain()
    }

    override suspend fun update(id: String, name: String, type: WalletType): Result<Unit> = resultOf {
        local.updateNameAndType(id = id, name = name.trim(), type = type, updatedAt = clock.now())
    }

    override suspend fun create(
        businessId: String,
        name: String,
        type: WalletType,
        openingBalance: Money,
    ): Result<Wallet> = resultOf {
        val now = clock.now()
        val wallet = Wallet(
            id = UUID.randomUUID().toString(),
            businessId = businessId,
            name = name.trim(),
            type = type,
            openingBalance = openingBalance,
            createdAt = now,
            updatedAt = now,
        )
        local.upsert(wallet.toEntity())
        wallet
    }

    override suspend fun deleteById(id: String): Result<Unit> = resultOf {
        local.deleteById(id)
    }
}
