package com.flowgroup.flowta.domain.usecase.transaction

import app.cash.turbine.test
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.common.WeekRange
import com.flowgroup.flowta.domain.model.Business
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.HealthPeriod
import com.flowgroup.flowta.domain.model.TransactionTotals
import com.flowgroup.flowta.domain.repository.BusinessRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveBusinessHealthForCurrentBusinessUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val businessRepository: BusinessRepository = mockk()
    private val preferencesRepository: PreferencesRepository = mockk()

    private val fixedNow: Instant = Instant.parse("2026-05-13T10:00:00Z")
    private val clock: Clock = mockk<Clock>().also { every { it.now() } returns fixedNow }

    private val useCase = ObserveBusinessHealthForCurrentBusinessUseCase(
        transactionRepository = transactionRepository,
        businessRepository = businessRepository,
        preferencesRepository = preferencesRepository,
        clock = clock,
    )

    private val business = Business(
        id = "biz-1",
        name = "Mama Lucy Kiosk",
        currency = CurrencyCode.KES,
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    @Test
    fun givenNoCurrentBusiness_whenObserved_thenEmitsSuccessNull() = runTest {
        every { preferencesRepository.currentBusinessId } returns flowOf(null)

        useCase().test {
            val item = awaitItem()
            assertTrue(item is Result.Success)
            assertNull((item as Result.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun givenBusinessAndTotals_whenObserved_thenComputesHealthWithDeltas() = runTest {
        stubTotals(
            currentTotals = TransactionTotals(salesMinor = 50_000, expensesMinor = 20_000),
            priorTotals = TransactionTotals(salesMinor = 40_000, expensesMinor = 25_000),
        )
        every { preferencesRepository.currentBusinessId } returns flowOf("biz-1")
        every { businessRepository.observeById("biz-1") } returns flowOf(Result.Success(business))

        useCase().test {
            val item = awaitItem()
            assertTrue(item is Result.Success)
            val health = (item as Result.Success).data
            assertNotNull(health)
            assertEquals(HealthPeriod.THIS_WEEK, health!!.period)
            assertEquals(50_000L, health.revenue.minorUnits)
            assertEquals(20_000L, health.expenses.minorUnits)
            assertEquals(CurrencyCode.KES, health.revenue.currency)
            // (50000 - 40000) / 40000 = +25.0%
            assertEquals(25.0, health.revenueDeltaPercent!!, 0.0001)
            // (20000 - 25000) / 25000 = -20.0%
            assertEquals(-20.0, health.expensesDeltaPercent!!, 0.0001)
            awaitComplete()
        }
    }

    @Test
    fun givenZeroPriorWeekTotals_whenObserved_thenDeltaIsNull() = runTest {
        stubTotals(
            currentTotals = TransactionTotals(salesMinor = 10_000, expensesMinor = 3_000),
            priorTotals = TransactionTotals.ZERO,
        )
        every { preferencesRepository.currentBusinessId } returns flowOf("biz-1")
        every { businessRepository.observeById("biz-1") } returns flowOf(Result.Success(business))

        useCase().test {
            val item = awaitItem()
            val health = (item as Result.Success).data!!
            assertNull(health.revenueDeltaPercent)
            assertNull(health.expensesDeltaPercent)
            awaitComplete()
        }
    }

    @Test
    fun givenBusinessRepoError_whenObserved_thenPropagatesError() = runTest {
        stubTotals(
            currentTotals = TransactionTotals.ZERO,
            priorTotals = TransactionTotals.ZERO,
        )
        every { preferencesRepository.currentBusinessId } returns flowOf("biz-1")
        every { businessRepository.observeById("biz-1") } returns
            flowOf(Result.Error(AppException.LocalException("boom")))

        useCase().test {
            val item = awaitItem()
            assertTrue(item is Result.Error)
            awaitComplete()
        }
    }

    private fun stubTotals(currentTotals: TransactionTotals, priorTotals: TransactionTotals) {
        val zone = TimeZone.currentSystemDefault()
        val current = WeekRange.thisWeek(fixedNow, zone)
        val prior = WeekRange.priorWeek(fixedNow, zone)
        every {
            transactionRepository.observeTotalsBetween("biz-1", any(), any())
        } answers {
            val start = secondArg<Instant>()
            when (start) {
                current.start -> flowOf(Result.Success(currentTotals))
                prior.start -> flowOf(Result.Success(priorTotals))
                else -> error("Unexpected start: $start")
            }
        }
    }
}
