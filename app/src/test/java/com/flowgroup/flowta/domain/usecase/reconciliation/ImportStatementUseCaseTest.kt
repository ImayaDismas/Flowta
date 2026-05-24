package com.flowgroup.flowta.domain.usecase.reconciliation

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Business
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.PaymentDirection
import com.flowgroup.flowta.domain.model.PaymentSource
import com.flowgroup.flowta.domain.reconciliation.StatementParserEngine
import com.flowgroup.flowta.domain.reconciliation.statement.MpesaStatementCsvParser
import com.flowgroup.flowta.domain.repository.BusinessRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.ReconciliationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportStatementUseCaseTest {

    private val engine = StatementParserEngine(setOf(MpesaStatementCsvParser()))
    private val reconciliationRepository: ReconciliationRepository = mockk()
    private val businessRepository: BusinessRepository = mockk()
    private val preferencesRepository: PreferencesRepository = mockk()
    private val useCase = ImportStatementUseCase(
        engine, reconciliationRepository, businessRepository, preferencesRepository,
    )

    private val business = Business(
        id = "biz-1",
        name = "Duka",
        currency = CurrencyCode.KES,
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    private val csv = """
        Receipt No.,Completion Time,Details,Transaction Status,Paid In,Withdrawn,Balance
        SGR45TXKLP,2026-05-24 13:15:00,Funds received from JOHN DOE 254712345678,Completed,"1,234.00",,28429.00
        DEF456GHJ7,2026-05-24 15:00:00,Pay Bill to KPLC,Completed,,200.00,28229.00
    """.trimIndent()

    @Test
    fun givenStatement_whenImported_thenStoresInflowsAndOutflows() = runTest {
        every { preferencesRepository.currentBusinessId } returns flowOf("biz-1")
        coEvery { businessRepository.getById("biz-1") } returns Result.Success(business)
        coEvery {
            reconciliationRepository.storeParsed("biz-1", any(), PaymentSource.STATEMENT_IMPORT)
        } returns Result.Success(2)

        val result = useCase(csv)

        assertTrue(result is Result.Success)
        val outcome = (result as Result.Success).data
        assertEquals(2, outcome.recognized) // Paid-In row (IN) + Withdrawn row (OUT)
        assertEquals(2, outcome.stored)
        coVerify {
            reconciliationRepository.storeParsed(
                "biz-1",
                match { parsed ->
                    parsed.size == 2 &&
                        parsed[0].reference == "SGR45TXKLP" &&
                        parsed[0].direction == PaymentDirection.IN &&
                        parsed[1].reference == "DEF456GHJ7" &&
                        parsed[1].direction == PaymentDirection.OUT
                },
                PaymentSource.STATEMENT_IMPORT,
            )
        }
    }

    @Test
    fun givenNoBusiness_whenImported_thenError() = runTest {
        every { preferencesRepository.currentBusinessId } returns flowOf(null)
        assertTrue(useCase(csv) is Result.Error)
    }

    @Test
    fun givenUnrecognisedCsv_whenImported_thenErrorAndNothingStored() = runTest {
        every { preferencesRepository.currentBusinessId } returns flowOf("biz-1")
        coEvery { businessRepository.getById("biz-1") } returns Result.Success(business)

        val result = useCase("Date,Description,Amount\n2026-05-24,Lunch,500")

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { reconciliationRepository.storeParsed(any(), any(), any()) }
    }
}
