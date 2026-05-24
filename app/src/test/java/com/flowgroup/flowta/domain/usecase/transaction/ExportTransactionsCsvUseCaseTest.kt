package com.flowgroup.flowta.domain.usecase.transaction

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.TransactionWithWallet
import com.flowgroup.flowta.domain.model.WalletType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportTransactionsCsvUseCaseTest {

    private val observeHistory: ObserveHistoryForCurrentBusinessUseCase = mockk()
    private val useCase = ExportTransactionsCsvUseCase(observeHistory)

    private fun txn(id: String, type: TransactionType, amount: Long, note: String?) =
        TransactionWithWallet(
            transaction = Transaction(
                id = id,
                businessId = "biz-1",
                walletId = "w-1",
                type = type,
                amount = Money(amount, CurrencyCode.KES),
                note = note,
                occurredAt = Instant.parse("2026-05-24T09:00:00Z"),
                createdAt = Instant.parse("2026-05-24T09:00:00Z"),
                updatedAt = Instant.parse("2026-05-24T09:00:00Z"),
            ),
            walletName = if (type == TransactionType.SALE) "M-Pesa Till" else "Cash Drawer",
            walletType = WalletType.MPESA,
        )

    @Test
    fun givenTransactions_whenExporting_thenHeaderRowsAndWholeShillings() = runTest {
        every { observeHistory() } returns flowOf(
            Result.Success(
                listOf(
                    txn("t1", TransactionType.SALE, 2_500L, "Sold 3 items, cash"),
                    txn("t2", TransactionType.EXPENSE, 1_000L, null),
                ),
            ),
        )

        val result = useCase()

        assertTrue(result is Result.Success)
        val export = (result as Result.Success).data
        assertEquals(2, export.rowCount)
        assertTrue(export.content.startsWith("Date,Time,Type,Amount,Currency,Wallet,Note\r\n"))
        // Whole shillings (2500, not 250000), correct columns.
        assertTrue(export.content.contains(",SALE,2500,KES,M-Pesa Till,"))
        assertTrue(export.content.contains(",EXPENSE,1000,KES,Cash Drawer,"))
        // A note containing a comma must be quoted.
        assertTrue(export.content.contains("\"Sold 3 items, cash\""))
    }

    @Test
    fun givenNoTransactions_whenExporting_thenHeaderOnlyAndZeroRows() = runTest {
        every { observeHistory() } returns flowOf(Result.Success(emptyList()))

        val export = (useCase() as Result.Success).data

        assertEquals(0, export.rowCount)
        assertEquals("Date,Time,Type,Amount,Currency,Wallet,Note\r\n", export.content)
    }
}
