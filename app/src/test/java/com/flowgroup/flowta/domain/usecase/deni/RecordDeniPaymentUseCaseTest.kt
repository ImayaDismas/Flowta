package com.flowgroup.flowta.domain.usecase.deni

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Client
import com.flowgroup.flowta.domain.model.DeniEntryType
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.repository.DeniRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordDeniPaymentUseCaseTest {

    private val deniRepository: DeniRepository = mockk()
    private val useCase = RecordDeniPaymentUseCase(deniRepository)

    private val client = Client(
        id = "c-1",
        businessId = "biz-1",
        name = "Mama Achieng",
        phone = null,
        currency = CurrencyCode.KES,
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    @Test
    fun givenZeroAmount_whenInvoked_thenErrorAndClientNotFetched() = runTest {
        val result = useCase(clientId = "c-1", amountMinor = 0L, note = null, walletId = null)

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { deniRepository.getClient(any()) }
    }

    @Test
    fun givenValidAmount_whenInvoked_thenPaymentEntryRecordedInClientCurrency() = runTest {
        coEvery { deniRepository.getClient("c-1") } returns Result.Success(client)
        coEvery {
            deniRepository.recordEntry("biz-1", "c-1", DeniEntryType.PAYMENT, Money(300L, CurrencyCode.KES), "part payment", null)
        } returns Result.Success(mockk())

        val result = useCase(clientId = "c-1", amountMinor = 300L, note = "part payment", walletId = null)

        assertTrue(result is Result.Success)
        coVerify {
            deniRepository.recordEntry("biz-1", "c-1", DeniEntryType.PAYMENT, Money(300L, CurrencyCode.KES), "part payment", null)
        }
    }
}
