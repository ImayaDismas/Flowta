package com.flowgroup.flowta.domain.usecase.wallet

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.repository.TransactionRepository
import com.flowgroup.flowta.domain.repository.WalletRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteWalletUseCaseTest {

    private val walletRepository: WalletRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val useCase = DeleteWalletUseCase(walletRepository, transactionRepository)

    @Test
    fun givenNoTransactions_whenInvoked_thenDeletedAndRepositoryDeleteCalled() = runTest {
        coEvery { transactionRepository.countForWallet("w-1") } returns Result.Success(0)
        coEvery { walletRepository.deleteById("w-1") } returns Result.Success(Unit)

        val outcome = useCase("w-1")

        assertTrue(outcome is DeleteWalletUseCase.Outcome.Deleted)
        coVerify { walletRepository.deleteById("w-1") }
    }

    @Test
    fun givenWalletHasTransactions_whenInvoked_thenBlockedWithCountAndDeleteNotCalled() = runTest {
        coEvery { transactionRepository.countForWallet("w-1") } returns Result.Success(3)

        val outcome = useCase("w-1")

        assertTrue(outcome is DeleteWalletUseCase.Outcome.Blocked)
        assertEquals(3, (outcome as DeleteWalletUseCase.Outcome.Blocked).transactionCount)
        coVerify(exactly = 0) { walletRepository.deleteById(any()) }
    }

    @Test
    fun givenCountFails_whenInvoked_thenFailedAndDeleteNotCalled() = runTest {
        coEvery { transactionRepository.countForWallet("w-1") } returns
            Result.Error(AppException.LocalException("count failed"))

        val outcome = useCase("w-1")

        assertTrue(outcome is DeleteWalletUseCase.Outcome.Failed)
        coVerify(exactly = 0) { walletRepository.deleteById(any()) }
    }

    @Test
    fun givenDeleteFails_whenInvoked_thenFailed() = runTest {
        coEvery { transactionRepository.countForWallet("w-1") } returns Result.Success(0)
        coEvery { walletRepository.deleteById("w-1") } returns
            Result.Error(AppException.LocalException("disk error"))

        val outcome = useCase("w-1")

        assertTrue(outcome is DeleteWalletUseCase.Outcome.Failed)
    }
}
