package com.flowgroup.flowta.domain.usecase.reconciliation

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.PaymentSource
import com.flowgroup.flowta.domain.model.ReceivedPayment
import com.flowgroup.flowta.domain.model.ReconciliationStatus
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.TransactionWithWallet
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.repository.ReconciliationRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class SuggestMatchUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val reconciliationRepository: ReconciliationRepository = mockk()
    private val useCase = SuggestMatchUseCase(transactionRepository, reconciliationRepository)

    private val paidAt = Instant.parse("2026-05-24T10:00:00Z")
    private val payment = receivedPayment(amountMinor = 250_000L, occurredAt = paidAt)

    @Test
    fun givenExactAmountSales_whenSuggesting_thenPicksClosestInTime() = runTest {
        val near = sale("near", 250_000L, paidAt + 5.minutes)
        val far = sale("far", 250_000L, paidAt + 10.hours)
        givenSales(near, far)
        givenMatchedTransactionIds()

        val result = useCase(payment)

        assertTrue(result is Result.Success)
        assertEquals("near", (result as Result.Success).data?.id)
    }

    @Test
    fun givenOnlyOutOfWindowSale_whenSuggesting_thenNull() = runTest {
        givenSales(sale("old", 250_000L, paidAt + 72.hours))
        givenMatchedTransactionIds()

        val result = useCase(payment)

        assertEquals(null, (result as Result.Success).data)
    }

    @Test
    fun givenExactMatchAlreadyMatched_whenSuggesting_thenExcludesIt() = runTest {
        givenSales(sale("taken", 250_000L, paidAt + 1.minutes))
        givenMatchedTransactionIds("taken")

        val result = useCase(payment)

        assertNull((result as Result.Success).data)
    }

    @Test
    fun givenWrongAmount_whenSuggesting_thenNull() = runTest {
        givenSales(sale("wrong", 100_000L, paidAt))
        givenMatchedTransactionIds()

        val result = useCase(payment)

        assertNull((result as Result.Success).data)
    }

    private fun givenSales(vararg sales: Transaction) {
        every { transactionRepository.observeHistoryForBusiness("biz-1") } returns
            flowOf(Result.Success(sales.map { TransactionWithWallet(it, "M-Pesa Till", WalletType.MPESA) }))
    }

    private fun givenMatchedTransactionIds(vararg ids: String) {
        val payments = ids.map { receivedPayment(amountMinor = 250_000L, occurredAt = paidAt, matchedTo = it) }
        every { reconciliationRepository.observeForBusiness("biz-1") } returns
            flowOf(Result.Success(payments))
    }

    private fun sale(id: String, amountMinor: Long, at: Instant) = Transaction(
        id = id,
        businessId = "biz-1",
        walletId = "w-1",
        type = TransactionType.SALE,
        amount = Money(amountMinor, CurrencyCode.KES),
        note = null,
        occurredAt = at,
        createdAt = at,
        updatedAt = at,
    )

    private fun receivedPayment(amountMinor: Long, occurredAt: Instant, matchedTo: String? = null) =
        ReceivedPayment(
            id = "p-1",
            businessId = "biz-1",
            provider = MobileMoneyProvider.MPESA,
            amount = Money(amountMinor, CurrencyCode.KES),
            reference = "TIX4A2B9P",
            senderName = "MARY WANJIKU",
            senderPhone = "254712345678",
            status = if (matchedTo != null) ReconciliationStatus.MATCHED else ReconciliationStatus.UNMATCHED,
            matchedTransactionId = matchedTo,
            source = PaymentSource.SMS_PASTE,
            occurredAt = occurredAt,
            createdAt = occurredAt,
            updatedAt = occurredAt,
        )
}
