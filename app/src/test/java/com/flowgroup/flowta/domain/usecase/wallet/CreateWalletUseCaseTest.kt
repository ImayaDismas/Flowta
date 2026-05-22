package com.flowgroup.flowta.domain.usecase.wallet

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Wallet
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.WalletRepository
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

class CreateWalletUseCaseTest {

    private val walletRepository: WalletRepository = mockk()
    private val preferencesRepository: PreferencesRepository = mockk()
    private val useCase = CreateWalletUseCase(walletRepository, preferencesRepository)

    @Test
    fun givenCurrentBusiness_whenInvoked_thenWalletIsCreatedForThatBusiness() = runTest {
        val opening = Money(minorUnits = 1_500L, currency = CurrencyCode.KES)
        val created = Wallet(
            id = "wallet-1",
            businessId = "business-1",
            name = "Cash drawer",
            type = WalletType.CASH,
            openingBalance = opening,
            createdAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )
        every { preferencesRepository.currentBusinessId } returns flowOf("business-1")
        coEvery {
            walletRepository.create("business-1", "Cash drawer", WalletType.CASH, opening)
        } returns Result.Success(created)

        val result = useCase("Cash drawer", WalletType.CASH, opening)

        assertTrue(result is Result.Success)
        assertEquals(created, (result as Result.Success).data)
    }

    @Test
    fun givenNoCurrentBusiness_whenInvoked_thenLocalErrorIsReturnedAndRepositoryIsNotCalled() = runTest {
        val opening = Money(minorUnits = 0L, currency = CurrencyCode.KES)
        every { preferencesRepository.currentBusinessId } returns flowOf(null)

        val result = useCase("Cash drawer", WalletType.CASH, opening)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.LocalException)
        coVerify(exactly = 0) {
            walletRepository.create(any(), any(), any(), any())
        }
    }
}
