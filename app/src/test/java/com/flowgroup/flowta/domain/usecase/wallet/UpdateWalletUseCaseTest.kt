package com.flowgroup.flowta.domain.usecase.wallet

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.repository.WalletRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateWalletUseCaseTest {

    private val walletRepository: WalletRepository = mockk()
    private val useCase = UpdateWalletUseCase(walletRepository)

    @Test
    fun givenValidNameAndType_whenInvoked_thenRepositoryIsCalledWithTrimmedName() = runTest {
        coEvery { walletRepository.update("w-1", "Cash drawer", WalletType.CASH) } returns
            Result.Success(Unit)

        val result = useCase("w-1", "  Cash drawer  ", WalletType.CASH)

        assertTrue(result is Result.Success)
        coVerify { walletRepository.update("w-1", "Cash drawer", WalletType.CASH) }
    }

    @Test
    fun givenBlankName_whenInvoked_thenLocalErrorAndRepositoryNotCalled() = runTest {
        val result = useCase("w-1", "   ", WalletType.CASH)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.LocalException)
        coVerify(exactly = 0) { walletRepository.update(any(), any(), any()) }
    }

    @Test
    fun givenNameOver80Chars_whenInvoked_thenLocalErrorAndRepositoryNotCalled() = runTest {
        val longName = "a".repeat(81)

        val result = useCase("w-1", longName, WalletType.CASH)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.LocalException)
        coVerify(exactly = 0) { walletRepository.update(any(), any(), any()) }
    }

    @Test
    fun givenRepositoryFails_whenInvoked_thenErrorIsPropagated() = runTest {
        coEvery { walletRepository.update(any(), any(), any()) } returns
            Result.Error(AppException.LocalException("disk full"))

        val result = useCase("w-1", "Cash", WalletType.CASH)

        assertTrue(result is Result.Error)
    }
}
