package com.flowgroup.flowta.domain.usecase.transaction

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Business
import com.flowgroup.flowta.domain.model.Client
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.DeniEntryType
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.repository.BusinessRepository
import com.flowgroup.flowta.domain.repository.DeniRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordSaleOnCreditUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val deniRepository: DeniRepository = mockk()
    private val businessRepository: BusinessRepository = mockk()
    private val preferencesRepository: PreferencesRepository = mockk()
    private val useCase = RecordSaleOnCreditUseCase(
        transactionRepository,
        deniRepository,
        businessRepository,
        preferencesRepository,
    )

    private val business = Business(
        id = "b-1",
        name = "Duka",
        currency = CurrencyCode.KES,
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    private fun stubBusiness() {
        every { preferencesRepository.currentBusinessId } returns flowOf("b-1")
        coEvery { businessRepository.getById("b-1") } returns Result.Success(business)
    }

    @Test
    fun givenExistingClientAndPartialCredit_whenInvoked_thenRecordsFullSaleAndCreditPortion() = runTest {
        stubBusiness()
        coEvery {
            transactionRepository.record("b-1", "w-1", TransactionType.SALE, Money(1000L, CurrencyCode.KES), "Maziwa")
        } returns Result.Success(mockk(relaxed = true))
        coEvery {
            deniRepository.recordEntry("b-1", "c-1", DeniEntryType.CREDIT, Money(600L, CurrencyCode.KES), "Maziwa", "w-1")
        } returns Result.Success(mockk(relaxed = true))

        val result = useCase(
            walletId = "w-1",
            totalAmountMinor = 1000L,
            creditAmountMinor = 600L,
            note = "Maziwa",
            client = SaleCreditClient.Existing("c-1"),
        )

        assertTrue(result is Result.Success)
        coVerify {
            transactionRepository.record("b-1", "w-1", TransactionType.SALE, Money(1000L, CurrencyCode.KES), "Maziwa")
        }
        coVerify {
            deniRepository.recordEntry("b-1", "c-1", DeniEntryType.CREDIT, Money(600L, CurrencyCode.KES), "Maziwa", "w-1")
        }
    }

    @Test
    fun givenNewClient_whenInvoked_thenCreatesClientThenRecordsSaleAndCredit() = runTest {
        stubBusiness()
        val created = Client(
            id = "c-new",
            businessId = "b-1",
            name = "Juma",
            phone = "0712",
            currency = CurrencyCode.KES,
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )
        coEvery { deniRepository.addClient("b-1", "Juma", "0712", CurrencyCode.KES) } returns Result.Success(created)
        coEvery {
            transactionRepository.record("b-1", "w-1", TransactionType.SALE, Money(800L, CurrencyCode.KES), null)
        } returns Result.Success(mockk(relaxed = true))
        coEvery {
            deniRepository.recordEntry("b-1", "c-new", DeniEntryType.CREDIT, Money(800L, CurrencyCode.KES), null, "w-1")
        } returns Result.Success(mockk(relaxed = true))

        val result = useCase(
            walletId = "w-1",
            totalAmountMinor = 800L,
            creditAmountMinor = 800L,
            note = null,
            client = SaleCreditClient.New("Juma", "0712"),
        )

        assertTrue(result is Result.Success)
        coVerify { deniRepository.addClient("b-1", "Juma", "0712", CurrencyCode.KES) }
        coVerify {
            deniRepository.recordEntry("b-1", "c-new", DeniEntryType.CREDIT, Money(800L, CurrencyCode.KES), null, "w-1")
        }
    }

    @Test
    fun givenCreditExceedsTotal_whenInvoked_thenErrorAndNothingRecorded() = runTest {
        val result = useCase(
            walletId = "w-1",
            totalAmountMinor = 500L,
            creditAmountMinor = 900L,
            note = null,
            client = SaleCreditClient.Existing("c-1"),
        )

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.LocalException)
        coVerify(exactly = 0) { transactionRepository.record(any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { deniRepository.recordEntry(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun givenZeroCredit_whenInvoked_thenErrorAndNothingRecorded() = runTest {
        val result = useCase(
            walletId = "w-1",
            totalAmountMinor = 500L,
            creditAmountMinor = 0L,
            note = null,
            client = SaleCreditClient.Existing("c-1"),
        )

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { transactionRepository.record(any(), any(), any(), any(), any()) }
    }

    @Test
    fun givenNoCurrentBusiness_whenInvoked_thenErrorAndNothingRecorded() = runTest {
        every { preferencesRepository.currentBusinessId } returns flowOf(null)

        val result = useCase(
            walletId = "w-1",
            totalAmountMinor = 500L,
            creditAmountMinor = 200L,
            note = null,
            client = SaleCreditClient.Existing("c-1"),
        )

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { transactionRepository.record(any(), any(), any(), any(), any()) }
    }
}
