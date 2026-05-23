package com.flowgroup.flowta.domain.usecase.deni

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Customer
import com.flowgroup.flowta.domain.model.DeniEntryType
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.repository.BusinessRepository
import com.flowgroup.flowta.domain.repository.DeniRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class AddCustomerUseCase @Inject constructor(
    private val deniRepository: DeniRepository,
    private val businessRepository: BusinessRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(
        name: String,
        phone: String?,
        initialCreditMinor: Long,
    ): Result<Customer> {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return Result.Error(AppException.LocalException("Name cannot be blank"))
        }
        if (trimmed.length > MAX_NAME_LENGTH) {
            return Result.Error(AppException.LocalException("Name too long"))
        }
        val businessId = preferencesRepository.currentBusinessId.firstOrNull()
            ?: return Result.Error(AppException.LocalException("No current business selected"))
        val business = when (val r = businessRepository.getById(businessId)) {
            is Result.Success -> r.data
                ?: return Result.Error(AppException.LocalException("Business not found"))
            is Result.Error -> return r
        }

        val customerResult = deniRepository.addCustomer(businessId, trimmed, phone, business.currency)
        val customer = when (customerResult) {
            is Result.Success -> customerResult.data
            is Result.Error -> return customerResult
        }

        if (initialCreditMinor > 0L) {
            val entryResult = deniRepository.recordEntry(
                businessId = businessId,
                customerId = customer.id,
                type = DeniEntryType.CREDIT,
                amount = Money(initialCreditMinor, business.currency),
                note = null,
            )
            if (entryResult is Result.Error) return entryResult
        }
        return Result.Success(customer)
    }

    private companion object { const val MAX_NAME_LENGTH = 80 }
}
