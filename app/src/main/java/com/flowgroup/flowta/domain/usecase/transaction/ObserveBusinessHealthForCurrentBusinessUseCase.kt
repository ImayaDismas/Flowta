package com.flowgroup.flowta.domain.usecase.transaction

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.common.WeekRange
import com.flowgroup.flowta.domain.model.BusinessHealth
import com.flowgroup.flowta.domain.model.HealthPeriod
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.repository.BusinessRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import javax.inject.Inject

class ObserveBusinessHealthForCurrentBusinessUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val businessRepository: BusinessRepository,
    private val preferencesRepository: PreferencesRepository,
    private val clock: Clock,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Result<BusinessHealth?>> =
        preferencesRepository.currentBusinessId.flatMapLatest { businessId ->
            if (businessId == null) {
                flowOf(Result.Success(null))
            } else {
                val zone = TimeZone.currentSystemDefault()
                val now = clock.now()
                val current = WeekRange.thisWeek(now, zone)
                val prior = WeekRange.priorWeek(now, zone)
                combine(
                    businessRepository.observeById(businessId),
                    transactionRepository.observeTotalsBetween(businessId, current.start, current.endExclusive),
                    transactionRepository.observeTotalsBetween(businessId, prior.start, prior.endExclusive),
                ) { businessResult, currentTotalsResult, priorTotalsResult ->
                    if (businessResult is Result.Error) return@combine businessResult
                    if (currentTotalsResult is Result.Error) return@combine currentTotalsResult
                    if (priorTotalsResult is Result.Error) return@combine priorTotalsResult

                    val business = (businessResult as Result.Success).data
                    val currentTotals = (currentTotalsResult as Result.Success).data
                    val priorTotals = (priorTotalsResult as Result.Success).data

                    if (business == null) {
                        Result.Success(null)
                    } else {
                        Result.Success(
                            BusinessHealth(
                                period = HealthPeriod.THIS_WEEK,
                                revenue = Money(currentTotals.salesMinor, business.currency),
                                expenses = Money(currentTotals.expensesMinor, business.currency),
                                revenueDeltaPercent = deltaPercent(currentTotals.salesMinor, priorTotals.salesMinor),
                                expensesDeltaPercent = deltaPercent(currentTotals.expensesMinor, priorTotals.expensesMinor),
                            )
                        )
                    }
                }
            }
        }

    private fun deltaPercent(current: Long, prior: Long): Double? {
        if (prior == 0L) return null
        return ((current - prior).toDouble() / prior.toDouble()) * 100.0
    }
}
