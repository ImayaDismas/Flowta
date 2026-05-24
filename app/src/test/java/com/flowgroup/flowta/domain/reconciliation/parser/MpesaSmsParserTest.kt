package com.flowgroup.flowta.domain.reconciliation.parser

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.PaymentDirection
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MpesaSmsParserTest {

    private val parser = MpesaSmsParser()

    private val received =
        "TIX4A2B9P Confirmed. You have received Ksh2,500.00 from MARY WANJIKU 254712345678 " +
            "on 24/5/26 at 1:15 PM. New M-PESA balance is Ksh12,500.00. " +
            "Transaction cost, Ksh0.00."

    private val sent =
        "QGH7X2K9P1 Confirmed. Ksh500.00 sent to JOHN DOE 254712345678 " +
            "on 24/5/26 at 1:15 PM. New M-PESA balance is Ksh12,000.00. Transaction cost, Ksh0.00."

    @Test
    fun givenReceivedMessage_whenCanParse_thenTrue() {
        assertTrue(parser.canParse(received))
    }

    @Test
    fun givenOutboundMessage_whenCanParse_thenTrue() {
        assertTrue(parser.canParse(sent))
    }

    @Test
    fun givenReceivedMessage_whenParse_thenExtractsReceivedAmountNotBalance() {
        val payment = parser.parse(received, CurrencyCode.KES)!!

        assertEquals(MobileMoneyProvider.MPESA, payment.provider)
        assertEquals(2_500L, payment.amount.minorUnits)
        assertEquals(CurrencyCode.KES, payment.amount.currency)
        assertEquals(PaymentDirection.IN, payment.direction)
    }

    @Test
    fun givenReceivedMessage_whenParse_thenExtractsSenderReferenceAndTime() {
        val payment = parser.parse(received, CurrencyCode.KES)!!

        assertEquals("TIX4A2B9P", payment.reference)
        assertEquals("MARY WANJIKU", payment.senderName)
        assertEquals("254712345678", payment.senderPhone)

        val expected = LocalDateTime(2026, 5, 24, 13, 15)
            .toInstant(TimeZone.of("Africa/Nairobi"))
        assertEquals(expected, payment.occurredAt)
    }

    @Test
    fun givenSentMessage_whenParse_thenOutboundWithRecipient() {
        val payment = parser.parse(sent, CurrencyCode.KES)!!

        assertEquals(PaymentDirection.OUT, payment.direction)
        assertEquals(500L, payment.amount.minorUnits)
        assertEquals("JOHN DOE", payment.senderName)
        assertEquals("254712345678", payment.senderPhone)
        assertEquals("QGH7X2K9P1", payment.reference)
    }

    @Test
    fun givenPaybillMessage_whenParse_thenOutboundIgnoringAccountClause() {
        val paybill =
            "QGH7X2K9P1 Confirmed. Ksh1,000.00 sent to KPLC PREPAID for account 12345 " +
                "on 24/5/26 at 1:15 PM. New M-PESA balance is Ksh4,000.00."

        val payment = parser.parse(paybill, CurrencyCode.KES)!!

        assertEquals(PaymentDirection.OUT, payment.direction)
        assertEquals(1_000L, payment.amount.minorUnits)
        assertEquals("KPLC PREPAID", payment.senderName)
        assertNull(payment.senderPhone)
    }

    @Test
    fun givenBuyGoodsMessage_whenParse_thenOutboundToTill() {
        val buyGoods =
            "QGH7X2K9P1 Confirmed. Ksh450.00 paid to NAIVAS LTD. " +
                "on 24/5/26 at 1:15 PM. New M-PESA balance is Ksh5,000.00."

        val payment = parser.parse(buyGoods, CurrencyCode.KES)!!

        assertEquals(PaymentDirection.OUT, payment.direction)
        assertEquals(450L, payment.amount.minorUnits)
        assertEquals("NAIVAS LTD", payment.senderName)
    }

    @Test
    fun givenWithdrawMessage_whenParse_thenOutbound() {
        val withdraw =
            "QWE12RTY34 Confirmed. on 24/5/26 at 1:15 PM Withdraw Ksh2,000.00 " +
                "from 123456 - GITHURAI AGENT New M-PESA balance is Ksh1,000.00."

        val payment = parser.parse(withdraw, CurrencyCode.KES)!!

        assertEquals(PaymentDirection.OUT, payment.direction)
        assertEquals(2_000L, payment.amount.minorUnits)
        assertEquals("123456 - GITHURAI AGENT", payment.senderName)
    }

    @Test
    fun givenUnrelatedText_whenParse_thenNull() {
        assertNull(parser.parse("Hello, this is not a payment", CurrencyCode.KES))
    }
}
