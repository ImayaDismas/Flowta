package com.flowgroup.flowta.domain.usecase.reconciliation

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Business
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.PaymentSource
import com.flowgroup.flowta.domain.reconciliation.PaymentSmsParserEngine
import com.flowgroup.flowta.domain.reconciliation.parser.AirtelMoneySmsParser
import com.flowgroup.flowta.domain.reconciliation.parser.MpesaSmsParser
import com.flowgroup.flowta.domain.reconciliation.parser.TkashSmsParser
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

class ParseAndStorePaymentsUseCaseTest {

    private val engine = PaymentSmsParserEngine(
        setOf(MpesaSmsParser(), AirtelMoneySmsParser(), TkashSmsParser()),
    )
    private val reconciliationRepository: ReconciliationRepository = mockk()
    private val businessRepository: BusinessRepository = mockk()
    private val preferencesRepository: PreferencesRepository = mockk()
    private val useCase = ParseAndStorePaymentsUseCase(
        engine, reconciliationRepository, businessRepository, preferencesRepository,
    )

    private val business = Business(
        id = "biz-1",
        name = "Duka",
        currency = CurrencyCode.KES,
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    private val mpesa =
        "TIX4A2B9P Confirmed. You have received Ksh2,500.00 from MARY WANJIKU 254712345678 " +
            "on 24/5/26 at 1:15 PM. New M-PESA balance is Ksh12,500.00."

    @Test
    fun givenRecognisedMessage_whenInvoked_thenStoresAndReportsOutcome() = runTest {
        every { preferencesRepository.currentBusinessId } returns flowOf("biz-1")
        coEvery { businessRepository.getById("biz-1") } returns Result.Success(business)
        coEvery {
            reconciliationRepository.storeParsed("biz-1", any(), PaymentSource.SMS_PASTE)
        } returns Result.Success(1)

        val result = useCase(listOf(mpesa), PaymentSource.SMS_PASTE)

        assertTrue(result is Result.Success)
        val outcome = (result as Result.Success).data
        assertEquals(1, outcome.submitted)
        assertEquals(1, outcome.recognized)
        assertEquals(1, outcome.stored)
    }

    @Test
    fun givenNoCurrentBusiness_whenInvoked_thenError() = runTest {
        every { preferencesRepository.currentBusinessId } returns flowOf(null)

        val result = useCase(listOf(mpesa), PaymentSource.SMS_PASTE)

        assertTrue(result is Result.Error)
    }

    @Test
    fun givenUnreadableMessage_whenInvoked_thenErrorAndNothingStored() = runTest {
        every { preferencesRepository.currentBusinessId } returns flowOf("biz-1")
        coEvery { businessRepository.getById("biz-1") } returns Result.Success(business)

        val result = useCase(listOf("just a hello, no payment here"), PaymentSource.SMS_PASTE)

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { reconciliationRepository.storeParsed(any(), any(), any()) }
    }
}
