package com.flowgroup.flowta.domain.usecase.transaction

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteTransactionUseCaseTest {

    private val repository: TransactionRepository = mockk()
    private val useCase = DeleteTransactionUseCase(repository)

    @Test
    fun givenId_whenInvoked_thenDelegatesToRepository() = runTest {
        coEvery { repository.deleteById("t-1") } returns Result.Success(Unit)

        val result = useCase("t-1")

        assertTrue(result is Result.Success)
        coVerify { repository.deleteById("t-1") }
    }
}
