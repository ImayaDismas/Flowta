package com.flowgroup.flowta.domain.usecase.transaction

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
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

class RecordTransactionUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val preferencesRepository: PreferencesRepository = mockk()
    private val useCase = RecordTransactionUseCase(transactionRepository, preferencesRepository)

    private val sampleAmount = Money(minorUnits = 1_500L, currency = CurrencyCode.KES)

    @Test
    fun givenCurrentBusinessAndPositiveAmount_whenInvoked_thenTransactionRecorded() = runTest {
        val recorded = Transaction(
            id = "tx-1",
            businessId = "business-1",
            walletId = "wallet-1",
            type = TransactionType.SALE,
            amount = sampleAmount,
            note = "Sukari",
            occurredAt = Instant.fromEpochMilliseconds(0),
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )
        every { preferencesRepository.currentBusinessId } returns flowOf("business-1")
        coEvery {
            transactionRepository.record(
                "business-1", "wallet-1", TransactionType.SALE, sampleAmount, "Sukari",
            )
        } returns Result.Success(recorded)

        val result = useCase("wallet-1", TransactionType.SALE, sampleAmount, "Sukari")

        assertTrue(result is Result.Success)
        assertEquals(recorded, (result as Result.Success).data)
    }

    @Test
    fun givenZeroAmount_whenInvoked_thenLocalErrorAndRepositoryNotCalled() = runTest {
        val zero = Money(minorUnits = 0L, currency = CurrencyCode.KES)

        val result = useCase("wallet-1", TransactionType.SALE, zero, null)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.LocalException)
        coVerify(exactly = 0) {
            transactionRepository.record(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun givenNoCurrentBusiness_whenInvoked_thenLocalErrorAndRepositoryNotCalled() = runTest {
        every { preferencesRepository.currentBusinessId } returns flowOf(null)

        val result = useCase("wallet-1", TransactionType.SALE, sampleAmount, null)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.LocalException)
        coVerify(exactly = 0) {
            transactionRepository.record(any(), any(), any(), any(), any())
        }
    }
}
