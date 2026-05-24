package com.flowgroup.flowta.domain.usecase.reconciliation

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.ReconciliationStatus
import com.flowgroup.flowta.domain.model.ReconciliationSummary
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.ReconciliationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveReconciliationSummaryForCurrentBusinessUseCase @Inject constructor(
    private val reconciliationRepository: ReconciliationRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Result<ReconciliationSummary>> =
        preferencesRepository.currentBusinessId.flatMapLatest { id ->
            if (id == null) {
                flowOf(Result.Success(ReconciliationSummary(emptyList(), emptyList())))
            } else {
                reconciliationRepository.observeForBusiness(id).map { result ->
                    result.map { payments ->
                        ReconciliationSummary(
                            unmatched = payments.filter { it.status == ReconciliationStatus.UNMATCHED },
                            matched = payments.filter { it.status == ReconciliationStatus.MATCHED },
                        )
                    }
                }
            }
        }
}
