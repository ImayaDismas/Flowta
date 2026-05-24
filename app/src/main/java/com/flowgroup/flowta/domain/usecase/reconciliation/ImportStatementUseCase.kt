package com.flowgroup.flowta.domain.usecase.reconciliation

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.ParseOutcome
import com.flowgroup.flowta.domain.model.PaymentSource
import com.flowgroup.flowta.domain.reconciliation.StatementParserEngine
import com.flowgroup.flowta.domain.repository.BusinessRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.ReconciliationRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Imports a mobile-money statement (CSV text) for the current business: parses received-payment
 * rows in the business currency and stores them for reconciliation. Duplicates are dropped at the
 * storage layer, so re-importing the same statement is safe.
 */
class ImportStatementUseCase @Inject constructor(
    private val engine: StatementParserEngine,
    private val reconciliationRepository: ReconciliationRepository,
    private val businessRepository: BusinessRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(csvText: String): Result<ParseOutcome> {
        if (csvText.isBlank()) {
            return Result.Error(AppException.LocalException("The file is empty"))
        }
        val businessId = preferencesRepository.currentBusinessId.firstOrNull()
            ?: return Result.Error(AppException.LocalException("No current business selected"))
        val business = when (val r = businessRepository.getById(businessId)) {
            is Result.Success -> r.data
                ?: return Result.Error(AppException.LocalException("Business not found"))
            is Result.Error -> return r
        }

        val parsed = engine.parse(csvText, business.currency)
        if (parsed.isEmpty()) {
            return Result.Error(
                AppException.LocalException("No payments found. Is this an M-Pesa statement (CSV)?"),
            )
        }

        return when (val stored = reconciliationRepository.storeParsed(businessId, parsed, PaymentSource.STATEMENT_IMPORT)) {
            is Result.Success -> Result.Success(
                ParseOutcome(submitted = parsed.size, recognized = parsed.size, stored = stored.data),
            )
            is Result.Error -> stored
        }
    }
}
