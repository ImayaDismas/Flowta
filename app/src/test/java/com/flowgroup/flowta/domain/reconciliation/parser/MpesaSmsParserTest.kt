package com.flowgroup.flowta.domain.reconciliation.parser

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MpesaSmsParserTest {

    private val parser = MpesaSmsParser()

    private val received =
        "TIX4A2B9P Confirmed. You have received Ksh2,500.00 from MARY WANJIKU 254712345678 " +
            "on 24/5/26 at 1:15 PM. New M-PESA balance is Ksh12,500.00. " +
            "Transaction cost, Ksh0.00."

    @Test
    fun givenReceivedMessage_whenCanParse_thenTrue() {
        assertTrue(parser.canParse(received))
    }

    @Test
    fun givenOutboundMessage_whenCanParse_thenFalse() {
        val sent = "TIX4A2B9P Confirmed. Ksh500.00 sent to JOHN DOE 254712345678 on 24/5/26."
        assertFalse(parser.canParse(sent))
    }

    @Test
    fun givenReceivedMessage_whenParse_thenExtractsReceivedAmountNotBalance() {
        val payment = parser.parse(received, CurrencyCode.KES)!!

        assertEquals(MobileMoneyProvider.MPESA, payment.provider)
        assertEquals(2_500L, payment.amount.minorUnits)
        assertEquals(CurrencyCode.KES, payment.amount.currency)
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
    fun givenUnrelatedText_whenParse_thenNull() {
        assertNull(parser.parse("Hello, this is not a payment", CurrencyCode.KES))
    }
}
