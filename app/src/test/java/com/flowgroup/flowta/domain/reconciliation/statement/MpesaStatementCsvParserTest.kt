package com.flowgroup.flowta.domain.reconciliation.statement

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.PaymentDirection
import com.flowgroup.flowta.domain.reconciliation.StatementParserEngine
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MpesaStatementCsvParserTest {

    private val engine = StatementParserEngine(setOf(MpesaStatementCsvParser()))

    // Includes preamble rows before the header, a quoted amount with a comma, and a withdrawal row.
    private val csv = """
        M-PESA Statement,,,,,,
        Customer Name: JOHN MWANGI,,,,,,
        ,,,,,,
        Receipt No.,Completion Time,Details,Transaction Status,Paid In,Withdrawn,Balance
        SGR45TXKLP,2026-05-24 13:15:00,Funds received from JOHN DOE 254712345678,Completed,"1,234.00",,"28,429.00"
        ABC123XYZ9,2026-05-24 14:00:00,Customer Payment from MARY WANJIKU 254700111222,Completed,500.00,,28929.00
        DEF456GHJ7,2026-05-24 15:00:00,Pay Bill to KPLC PREPAID,Completed,,200.00,28729.00
    """.trimIndent()

    @Test
    fun givenStatement_whenParsing_thenPaidInAreInboundAndWithdrawnOutbound() {
        val payments = engine.parse(csv, CurrencyCode.KES)

        // Two Paid In rows (IN) and one Withdrawn row (OUT); only charge/zero rows are dropped.
        assertEquals(3, payments.size)
        assertEquals(listOf(PaymentDirection.IN, PaymentDirection.IN, PaymentDirection.OUT), payments.map { it.direction })

        val withdrawn = payments[2]
        assertEquals("DEF456GHJ7", withdrawn.reference)
        assertEquals(200L, withdrawn.amount.minorUnits)
        assertEquals("KPLC PREPAID", withdrawn.senderName)
    }

    @Test
    fun givenStatement_whenParsing_thenFieldsExtractedInWholeShillings() {
        val payments = engine.parse(csv, CurrencyCode.KES)

        val first = payments[0]
        assertEquals(MobileMoneyProvider.MPESA, first.provider)
        assertEquals("SGR45TXKLP", first.reference)
        assertEquals(1_234L, first.amount.minorUnits)
        assertEquals("JOHN DOE", first.senderName)
        assertEquals("254712345678", first.senderPhone)
        assertEquals(PaymentDirection.IN, first.direction)
        assertEquals(Instant.parse("2026-05-24T10:15:00Z"), first.occurredAt)

        val second = payments[1]
        assertEquals("ABC123XYZ9", second.reference)
        assertEquals(500L, second.amount.minorUnits)
        assertEquals("MARY WANJIKU", second.senderName)
    }

    @Test
    fun givenUnrecognisedCsv_whenParsing_thenEmpty() {
        val other = "Date,Description,Amount\n2026-05-24,Lunch,500"
        assertTrue(engine.parse(other, CurrencyCode.KES).isEmpty())
    }

    @Test
    fun givenQuotedAmountWithComma_whenTokenising_thenSingleField() {
        val rows = parseCsv("a,\"1,234.00\",b")
        assertEquals(listOf("a", "1,234.00", "b"), rows[0])
    }

    @Test
    fun givenBlankText_whenParsing_thenEmpty() {
        assertNull(engine.parse("", CurrencyCode.KES).firstOrNull())
    }
}
