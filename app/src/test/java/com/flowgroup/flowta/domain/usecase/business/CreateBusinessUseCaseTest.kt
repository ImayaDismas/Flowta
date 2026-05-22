package com.flowgroup.flowta.domain.usecase.business

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Business
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.repository.BusinessRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateBusinessUseCaseTest {

    private val businessRepository: BusinessRepository = mockk()
    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)
    private val useCase = CreateBusinessUseCase(businessRepository, preferencesRepository)

    @Test
    fun givenRepositorySucceeds_whenInvoked_thenBusinessIsCreatedAndSetAsCurrent() = runTest {
        val createdBusiness = Business(
            id = "business-1",
            name = "Mama Lucy Kiosk",
            currency = CurrencyCode.KES,
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )
        coEvery { businessRepository.create("Mama Lucy Kiosk", CurrencyCode.KES) } returns
            Result.Success(createdBusiness)

        val result = useCase("Mama Lucy Kiosk", CurrencyCode.KES)

        assertTrue(result is Result.Success)
        assertEquals(createdBusiness, (result as Result.Success).data)
        coVerify { preferencesRepository.setCurrentBusinessId("business-1") }
    }

    @Test
    fun givenRepositoryFails_whenInvoked_thenErrorIsReturnedAndCurrentBusinessNotSet() = runTest {
        // Note: MockK's `any()` matcher constructs a signature value by calling the constructor,
        // which fails CurrencyCode's ISO 4217 validation. Use a concrete value instead.
        val error = AppException.LocalException("disk full")
        coEvery { businessRepository.create(any(), CurrencyCode.KES) } returns Result.Error(error)

        val result = useCase("Mama Lucy Kiosk", CurrencyCode.KES)

        assertTrue(result is Result.Error)
        assertEquals(error, (result as Result.Error).exception)
        coVerify(exactly = 0) { preferencesRepository.setCurrentBusinessId(any()) }
    }
}
