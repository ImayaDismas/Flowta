package com.flowgroup.flowta.domain.usecase.transaction

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.DeniEntryType
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.repository.BusinessRepository
import com.flowgroup.flowta.domain.repository.DeniRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/** The client a credit sale is owed by — either one that already exists or a new one to create. */
sealed interface SaleCreditClient {
    data class Existing(val id: String) : SaleCreditClient
    data class New(val name: String, val phone: String?) : SaleCreditClient
}

/**
 * Records a sale where part (or all) is taken on credit.
 *
 * The full sale is recognised as revenue immediately, while only the paid portion is cash:
 * a SALE transaction for [totalAmountMinor] (revenue +total, wallet +total) plus a deni CREDIT
 * for [creditAmountMinor] linked to the same wallet (wallet −credit, client owed +credit). The
 * wallet therefore nets to the cash actually received (total − credit). Deni entries stay out of
 * the P&L, so recognising the full sale up front is what keeps the credit portion's revenue from
 * being lost when the client later pays.
 */
class RecordSaleOnCreditUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val deniRepository: DeniRepository,
    private val businessRepository: BusinessRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(
        walletId: String,
        totalAmountMinor: Long,
        creditAmountMinor: Long,
        note: String?,
        client: SaleCreditClient,
    ): Result<Unit> {
        if (totalAmountMinor <= 0L) {
            return Result.Error(AppException.LocalException("Amount must be greater than zero"))
        }
        if (creditAmountMinor <= 0L) {
            return Result.Error(AppException.LocalException("Credit amount must be greater than zero"))
        }
        if (creditAmountMinor > totalAmountMinor) {
            return Result.Error(AppException.LocalException("Credit cannot exceed the sale amount"))
        }

        val businessId = preferencesRepository.currentBusinessId.firstOrNull()
            ?: return Result.Error(AppException.LocalException("No current business selected"))
        val business = when (val r = businessRepository.getById(businessId)) {
            is Result.Success -> r.data
                ?: return Result.Error(AppException.LocalException("Business not found"))
            is Result.Error -> return r
        }
        val currency = business.currency

        val clientId = when (client) {
            is SaleCreditClient.Existing -> client.id
            is SaleCreditClient.New -> {
                val name = client.name.trim()
                if (name.isBlank()) {
                    return Result.Error(AppException.LocalException("Client name cannot be blank"))
                }
                when (val r = deniRepository.addClient(businessId, name, client.phone, currency)) {
                    is Result.Success -> r.data.id
                    is Result.Error -> return r
                }
            }
        }

        val saleResult = transactionRepository.record(
            businessId = businessId,
            walletId = walletId,
            type = TransactionType.SALE,
            amount = Money(totalAmountMinor, currency),
            note = note,
        )
        if (saleResult is Result.Error) return saleResult

        val creditResult = deniRepository.recordEntry(
            businessId = businessId,
            clientId = clientId,
            type = DeniEntryType.CREDIT,
            amount = Money(creditAmountMinor, currency),
            note = note,
            walletId = walletId,
        )
        if (creditResult is Result.Error) return creditResult

        return Result.Success(Unit)
    }
}
