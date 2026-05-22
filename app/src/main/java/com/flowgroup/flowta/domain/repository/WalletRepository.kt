package com.flowgroup.flowta.domain.repository

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Wallet
import com.flowgroup.flowta.domain.model.WalletType
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun observeForBusiness(businessId: String): Flow<Result<List<Wallet>>>
    suspend fun getById(id: String): Result<Wallet?>
    suspend fun create(
        businessId: String,
        name: String,
        type: WalletType,
        openingBalance: Money,
    ): Result<Wallet>
    suspend fun deleteById(id: String): Result<Unit>
}
