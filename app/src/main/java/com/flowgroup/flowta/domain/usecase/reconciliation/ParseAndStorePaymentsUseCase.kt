package com.flowgroup.flowta.domain.usecase.reconciliation

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.ParseOutcome
import com.flowgroup.flowta.domain.model.PaymentSource
import com.flowgroup.flowta.domain.reconciliation.PaymentSmsParserEngine
import com.flowgroup.flowta.domain.repository.BusinessRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.ReconciliationRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Parses raw provider messages (from any input method) in the current business's currency and
 * stores the recognised payments for reconciliation. Duplicates are dropped at the storage layer.
 */
class ParseAndStorePaymentsUseCase @Inject constructor(
    private val engine: PaymentSmsParserEngine,
    private val reconciliationRepository: ReconciliationRepository,
    private val businessRepository: BusinessRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(
        rawMessages: List<String>,
        source: PaymentSource,
    ): Result<ParseOutcome> {
        val messages = rawMessages.map { it.trim() }.filter { it.isNotEmpty() }
        if (messages.isEmpty()) {
            return Result.Error(AppException.LocalException("No message to read"))
        }
        val businessId = preferencesRepository.currentBusinessId.firstOrNull()
            ?: return Result.Error(AppException.LocalException("No current business selected"))
        val business = when (val r = businessRepository.getById(businessId)) {
            is Result.Success -> r.data
                ?: return Result.Error(AppException.LocalException("Business not found"))
            is Result.Error -> return r
        }

        val parsed = engine.parseAll(messages, business.currency)
        if (parsed.isEmpty()) {
            return Result.Error(AppException.LocalException("Could not read any payment from the message"))
        }

        return when (val stored = reconciliationRepository.storeParsed(businessId, parsed, source)) {
            is Result.Success -> Result.Success(
                ParseOutcome(
                    submitted = messages.size,
                    recognized = parsed.size,
                    stored = stored.data,
                ),
            )
            is Result.Error -> stored
        }
    }
}
