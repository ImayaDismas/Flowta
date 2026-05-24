package com.flowgroup.flowta.domain.repository

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Client
import com.flowgroup.flowta.domain.model.ClientDeni
import com.flowgroup.flowta.domain.model.DeniEntry
import com.flowgroup.flowta.domain.model.DeniEntryType
import com.flowgroup.flowta.domain.model.Money
import kotlinx.coroutines.flow.Flow

interface DeniRepository {
    fun observeClientsWithBalance(businessId: String): Flow<Result<List<ClientDeni>>>
    fun observeTotalOutstanding(businessId: String): Flow<Result<Long>>
    fun observeClientWithBalance(clientId: String): Flow<Result<ClientDeni?>>
    fun observeEntriesForClient(clientId: String): Flow<Result<List<DeniEntry>>>
    suspend fun getClient(clientId: String): Result<Client?>
    suspend fun addClient(
        businessId: String,
        name: String,
        phone: String?,
        currency: CurrencyCode,
    ): Result<Client>
    suspend fun recordEntry(
        businessId: String,
        clientId: String,
        type: DeniEntryType,
        amount: Money,
        note: String?,
        walletId: String?,
    ): Result<DeniEntry>
}
