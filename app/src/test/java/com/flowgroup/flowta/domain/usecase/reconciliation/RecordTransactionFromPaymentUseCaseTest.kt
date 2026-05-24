package com.flowgroup.flowta.domain.usecase.reconciliation

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.PaymentDirection
import com.flowgroup.flowta.domain.model.PaymentSource
import com.flowgroup.flowta.domain.model.ReceivedPayment
import com.flowgroup.flowta.domain.model.ReconciliationStatus
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.repository.ReconciliationRepository
import com.flowgroup.flowta.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordTransactionFromPaymentUseCaseTest {

    private val reconciliationRepository: ReconciliationRepository = mockk(relaxed = true)
    private val transactionRepository: TransactionRepository = mockk()
    private val useCase = RecordTransactionFromPaymentUseCase(reconciliationRepository, transactionRepository)

    private val at = Instant.parse("2026-05-24T10:00:00Z")

    @Test
    fun givenInboundPayment_whenRecording_thenRecordsSale() = runTest {
        val typeSlot = recordReturning(payment(PaymentDirection.IN))

        val result = useCase("p-1", "w-1")

        assertTrue(result is Result.Success)
        assertEquals(TransactionType.SALE, typeSlot.captured)
        coVerify { reconciliationRepository.matchToTransaction("p-1", "t-1") }
    }

    @Test
    fun givenOutboundPayment_whenRecording_thenRecordsExpense() = runTest {
        val typeSlot = recordReturning(payment(PaymentDirection.OUT))

        val result = useCase("p-1", "w-1")

        assertTrue(result is Result.Success)
        assertEquals(TransactionType.EXPENSE, typeSlot.captured)
        coVerify { reconciliationRepository.matchToTransaction("p-1", "t-1") }
    }

    private fun recordReturning(payment: ReceivedPayment): io.mockk.CapturingSlot<TransactionType> {
        coEvery { reconciliationRepository.getById("p-1") } returns Result.Success(payment)
        coEvery { reconciliationRepository.matchToTransaction(any(), any()) } returns Result.Success(Unit)
        val typeSlot = slot<TransactionType>()
        coEvery {
            transactionRepository.record(any(), any(), capture(typeSlot), any(), any())
        } returns Result.Success(transaction(payment.amount.minorUnits))
        return typeSlot
    }

    private fun payment(direction: PaymentDirection) = ReceivedPayment(
        id = "p-1",
        businessId = "biz-1",
        provider = MobileMoneyProvider.MPESA,
        amount = Money(500L, CurrencyCode.KES),
        reference = "TIX4A2B9P",
        senderName = "JOHN DOE",
        senderPhone = "254712345678",
        direction = direction,
        status = ReconciliationStatus.UNMATCHED,
        matchedTransactionId = null,
        source = PaymentSource.SMS_PASTE,
        occurredAt = at,
        createdAt = at,
        updatedAt = at,
    )

    private fun transaction(amountMinor: Long) = Transaction(
        id = "t-1",
        businessId = "biz-1",
        walletId = "w-1",
        type = TransactionType.SALE,
        amount = Money(amountMinor, CurrencyCode.KES),
        note = null,
        occurredAt = at,
        createdAt = at,
        updatedAt = at,
    )
}
