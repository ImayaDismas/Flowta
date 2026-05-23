package com.flowgroup.flowta.domain.repository

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Customer
import com.flowgroup.flowta.domain.model.CustomerDeni
import com.flowgroup.flowta.domain.model.DeniEntry
import com.flowgroup.flowta.domain.model.DeniEntryType
import com.flowgroup.flowta.domain.model.Money
import kotlinx.coroutines.flow.Flow

interface DeniRepository {
    fun observeCustomersWithBalance(businessId: String): Flow<Result<List<CustomerDeni>>>
    fun observeTotalOutstanding(businessId: String): Flow<Result<Long>>
    fun observeCustomerWithBalance(customerId: String): Flow<Result<CustomerDeni?>>
    fun observeEntriesForCustomer(customerId: String): Flow<Result<List<DeniEntry>>>
    suspend fun getCustomer(customerId: String): Result<Customer?>
    suspend fun addCustomer(
        businessId: String,
        name: String,
        phone: String?,
        currency: CurrencyCode,
    ): Result<Customer>
    suspend fun recordEntry(
        businessId: String,
        customerId: String,
        type: DeniEntryType,
        amount: Money,
        note: String?,
    ): Result<DeniEntry>
}
