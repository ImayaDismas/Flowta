package com.flowgroup.flowta.domain.usecase.deni

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.DeniEntry
import com.flowgroup.flowta.domain.model.DeniEntryType
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.repository.DeniRepository
import javax.inject.Inject

class RecordDeniPaymentUseCase @Inject constructor(
    private val deniRepository: DeniRepository,
) {
    suspend operator fun invoke(
        clientId: String,
        amountMinor: Long,
        note: String?,
    ): Result<DeniEntry> {
        if (amountMinor <= 0L) {
            return Result.Error(AppException.LocalException("Amount must be greater than zero"))
        }
        val client = when (val r = deniRepository.getClient(clientId)) {
            is Result.Success -> r.data
                ?: return Result.Error(AppException.LocalException("Client not found"))
            is Result.Error -> return r
        }
        return deniRepository.recordEntry(
            businessId = client.businessId,
            clientId = clientId,
            type = DeniEntryType.PAYMENT,
            amount = Money(amountMinor, client.currency),
            note = note,
        )
    }
}
