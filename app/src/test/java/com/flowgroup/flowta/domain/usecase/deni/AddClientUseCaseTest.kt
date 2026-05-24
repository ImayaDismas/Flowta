package com.flowgroup.flowta.domain.usecase.deni

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Business
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Client
import com.flowgroup.flowta.domain.model.DeniEntryType
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.repository.BusinessRepository
import com.flowgroup.flowta.domain.repository.DeniRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertTrue
import org.junit.Test

class AddClientUseCaseTest {

    private val deniRepository: DeniRepository = mockk()
    private val businessRepository: BusinessRepository = mockk()
    private val preferencesRepository: PreferencesRepository = mockk()
    private val useCase = AddClientUseCase(deniRepository, businessRepository, preferencesRepository)

    private val business = Business(
        id = "biz-1",
        name = "Mama Lucy Kiosk",
        currency = CurrencyCode.KES,
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    private val client = Client(
        id = "c-1",
        businessId = "biz-1",
        name = "Mama Achieng",
        phone = null,
        currency = CurrencyCode.KES,
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    private fun stubCurrentBusiness() {
        every { preferencesRepository.currentBusinessId } returns flowOf("biz-1")
        coEvery { businessRepository.getById("biz-1") } returns Result.Success(business)
    }

    @Test
    fun givenBlankName_whenInvoked_thenErrorAndNoClientCreated() = runTest {
        val result = useCase(name = "   ", phone = null, initialCreditMinor = 0L, walletId = null)

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { deniRepository.addClient(any(), any(), any(), CurrencyCode.KES) }
    }

    @Test
    fun givenInitialCredit_whenInvoked_thenClientCreatedAndCreditEntryRecorded() = runTest {
        stubCurrentBusiness()
        coEvery { deniRepository.addClient("biz-1", "Mama Achieng", null, CurrencyCode.KES) } returns
            Result.Success(client)
        coEvery {
            deniRepository.recordEntry("biz-1", "c-1", DeniEntryType.CREDIT, Money(500L, CurrencyCode.KES), null, null)
        } returns Result.Success(mockk())

        val result = useCase(name = "Mama Achieng", phone = null, initialCreditMinor = 500L, walletId = null)

        assertTrue(result is Result.Success)
        coVerify {
            deniRepository.recordEntry("biz-1", "c-1", DeniEntryType.CREDIT, Money(500L, CurrencyCode.KES), null, null)
        }
    }

    @Test
    fun givenNoInitialCredit_whenInvoked_thenNoEntryRecorded() = runTest {
        stubCurrentBusiness()
        coEvery { deniRepository.addClient("biz-1", "Mama Achieng", null, CurrencyCode.KES) } returns
            Result.Success(client)

        val result = useCase(name = "Mama Achieng", phone = null, initialCreditMinor = 0L, walletId = null)

        assertTrue(result is Result.Success)
        coVerify(exactly = 0) { deniRepository.recordEntry(any(), any(), any(), any(), any(), any()) }
    }
}
