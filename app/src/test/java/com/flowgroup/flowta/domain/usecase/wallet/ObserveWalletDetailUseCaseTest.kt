package com.flowgroup.flowta.domain.usecase.wallet

import app.cash.turbine.test
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.TransactionTotals
import com.flowgroup.flowta.domain.model.Wallet
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.model.WalletWithBalance
import com.flowgroup.flowta.domain.repository.DeniRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
import com.flowgroup.flowta.domain.repository.WalletRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveWalletDetailUseCaseTest {

    private val walletRepository: WalletRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val deniRepository: DeniRepository = mockk()
    private val fixedNow: Instant = Instant.parse("2026-05-13T10:00:00Z")
    private val clock: Clock = mockk<Clock>().also { every { it.now() } returns fixedNow }
    private val useCase = ObserveWalletDetailUseCase(walletRepository, transactionRepository, deniRepository, clock)

    private val wallet = Wallet(
        id = "w-1",
        businessId = "biz-1",
        name = "Cash drawer",
        type = WalletType.CASH,
        openingBalance = Money(2_000L, CurrencyCode.KES),
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )

    @Test
    fun givenWalletAndStreams_whenObserved_thenEmitsContentWithAuthoritativeBalance() = runTest {
        every { walletRepository.observeWithBalanceById("w-1") } returns flowOf(
            Result.Success(WalletWithBalance(wallet = wallet, currentBalanceMinor = 8_500L))
        )
        every { transactionRepository.observeRecentForWallet("w-1", any()) } returns
            flowOf(Result.Success(emptyList()))
        every { deniRepository.observeMovementsForWallet("w-1") } returns
            flowOf(Result.Success(emptyList()))
        every {
            transactionRepository.observeWalletTotalsBetween("w-1", any(), any())
        } returns flowOf(
            Result.Success(TransactionTotals(salesMinor = 7_000L, expensesMinor = 500L))
        )

        useCase("w-1").test {
            val item = awaitItem()
            assertTrue(item is Result.Success)
            val detail = (item as Result.Success).data
            assertNotNull(detail)
            assertEquals(wallet, detail!!.wallet)
            assertEquals(8_500L, detail.currentBalanceMinor)
            assertEquals(7_000L, detail.weekTotals.salesMinor)
            assertEquals(500L, detail.weekTotals.expensesMinor)
            awaitComplete()
        }
    }

    @Test
    fun givenWalletNotFound_whenObserved_thenEmitsSuccessNull() = runTest {
        every { walletRepository.observeWithBalanceById("w-1") } returns
            flowOf(Result.Success(null))
        every { transactionRepository.observeRecentForWallet("w-1", any()) } returns
            flowOf(Result.Success(emptyList()))
        every { deniRepository.observeMovementsForWallet("w-1") } returns
            flowOf(Result.Success(emptyList()))
        every {
            transactionRepository.observeWalletTotalsBetween("w-1", any(), any())
        } returns flowOf(Result.Success(TransactionTotals.ZERO))

        useCase("w-1").test {
            val item = awaitItem()
            assertTrue(item is Result.Success)
            assertNull((item as Result.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun givenWalletStreamError_whenObserved_thenErrorIsPropagated() = runTest {
        every { walletRepository.observeWithBalanceById("w-1") } returns
            flowOf(Result.Error(AppException.LocalException("read failed")))
        every { transactionRepository.observeRecentForWallet("w-1", any()) } returns
            flowOf(Result.Success(emptyList()))
        every { deniRepository.observeMovementsForWallet("w-1") } returns
            flowOf(Result.Success(emptyList()))
        every {
            transactionRepository.observeWalletTotalsBetween("w-1", any(), any())
        } returns flowOf(Result.Success(TransactionTotals.ZERO))

        useCase("w-1").test {
            val item = awaitItem()
            assertTrue(item is Result.Error)
            awaitComplete()
        }
    }
}
