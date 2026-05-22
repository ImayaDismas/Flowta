package com.flowgroup.flowta.domain.repository

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Business
import com.flowgroup.flowta.domain.model.CurrencyCode
import kotlinx.coroutines.flow.Flow

interface BusinessRepository {
    fun observeAll(): Flow<Result<List<Business>>>
    fun observeById(id: String): Flow<Result<Business?>>
    suspend fun getById(id: String): Result<Business?>
    suspend fun create(name: String, currency: CurrencyCode): Result<Business>
    suspend fun update(business: Business): Result<Unit>
    suspend fun deleteById(id: String): Result<Unit>
}