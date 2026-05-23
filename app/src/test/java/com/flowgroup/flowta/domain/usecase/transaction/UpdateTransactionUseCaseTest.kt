package com.flowgroup.flowta.domain.usecase.transaction

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateTransactionUseCaseTest {

    private val repository: TransactionRepository = mockk()
    private val useCase = UpdateTransactionUseCase(repository)

    @Test
    fun givenZeroAmount_whenInvoked_thenErrorAndRepositoryNotCalled() = runTest {
        val result = useCase(
            id = "t-1",
            walletId = "w-1",
            type = TransactionType.SALE,
            amount = Money(0L, CurrencyCode.KES),
            note = null,
        )

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { repository.update(any(), any(), any(), any(), any()) }
    }

    @Test
    fun givenValidAmount_whenInvoked_thenDelegatesToRepository() = runTest {
        val money = Money(1_500L, CurrencyCode.KES)
        coEvery {
            repository.update("t-1", "w-1", TransactionType.EXPENSE, money, "rent")
        } returns Result.Success(Unit)

        val result = useCase(
            id = "t-1",
            walletId = "w-1",
            type = TransactionType.EXPENSE,
            amount = money,
            note = "rent",
        )

        assertTrue(result is Result.Success)
        coVerify { repository.update("t-1", "w-1", TransactionType.EXPENSE, money, "rent") }
    }
}
