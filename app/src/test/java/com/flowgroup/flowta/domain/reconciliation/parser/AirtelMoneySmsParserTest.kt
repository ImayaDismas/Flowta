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

class AirtelMoneySmsParserTest {

    private val parser = AirtelMoneySmsParser()

    private val received =
        "TID PP240524.1315.A12345 Confirmed. You have received KES 500.00 from JOHN OTIENO " +
            "254731234567 on 24/05/2026 at 01:15 PM. Your Airtel Money balance is KES 1,000.00."

    private val sent =
        "TID PP240524.1315.B67890 Confirmed. You have sent KES 800.00 to PETER SUPPLIER " +
            "254731234568 on 24/05/2026 at 02:30 PM. Your Airtel Money balance is KES 200.00."

    private val paid =
        "TID PP240524.1315.C11111 Confirmed. You have paid KES 250.00 to NAIVAS LTD " +
            "on 24/05/2026 at 03:00 PM. Your Airtel Money balance is KES 750.00."

    private val withdrawn =
        "TID PP240524.1315.D22222 Confirmed. You have withdrawn KES 2,000.00 from GITHURAI AGENT " +
            "254731234569 on 24/05/2026 at 04:00 PM. Your Airtel Money balance is KES 500.00."

    // ── canParse ──────────────────────────────────────────────────────────────

    @Test
    fun givenAirtelReceivedMessage_whenCanParse_thenTrue() {
        assertTrue(parser.canParse(received))
    }

    @Test
    fun givenAirtelSentMessage_whenCanParse_thenTrue() {
        assertTrue(parser.canParse(sent))
    }

    @Test
    fun givenAirtelPaidMessage_whenCanParse_thenTrue() {
        assertTrue(parser.canParse(paid))
    }

    @Test
    fun givenAirtelWithdrawnMessage_whenCanParse_thenTrue() {
        assertTrue(parser.canParse(withdrawn))
    }

    // ── inbound (IN) ──────────────────────────────────────────────────────────

    @Test
    fun givenAirtelReceivedMessage_whenParse_thenExtractsAllFields() {
        val payment = parser.parse(received, CurrencyCode.KES)!!

        assertEquals(MobileMoneyProvider.AIRTEL_MONEY, payment.provider)
        assertEquals(500L, payment.amount.minorUnits)
        assertEquals("PP240524.1315.A12345", payment.reference)
        assertEquals("JOHN OTIENO", payment.senderName)
        assertEquals("254731234567", payment.senderPhone)
        assertEquals(PaymentDirection.IN, payment.direction)

        val expected = LocalDateTime(2026, 5, 24, 13, 15)
            .toInstant(TimeZone.of("Africa/Nairobi"))
        assertEquals(expected, payment.occurredAt)
    }

    // ── outbound (OUT) — send to person ───────────────────────────────────────

    @Test
    fun givenAirtelSentMessage_whenParse_thenOutboundWithRecipient() {
        val payment = parser.parse(sent, CurrencyCode.KES)!!

        assertEquals(PaymentDirection.OUT, payment.direction)
        assertEquals(800L, payment.amount.minorUnits)
        assertEquals("PETER SUPPLIER", payment.senderName)
        assertEquals("254731234568", payment.senderPhone)
        assertEquals("PP240524.1315.B67890", payment.reference)
    }

    // ── outbound (OUT) — pay merchant ─────────────────────────────────────────

    @Test
    fun givenAirtelPaidMessage_whenParse_thenOutboundToMerchant() {
        val payment = parser.parse(paid, CurrencyCode.KES)!!

        assertEquals(PaymentDirection.OUT, payment.direction)
        assertEquals(250L, payment.amount.minorUnits)
        assertEquals("NAIVAS LTD", payment.senderName)
        assertNull(payment.senderPhone)
    }

    // ── outbound (OUT) — cash withdrawal ──────────────────────────────────────

    @Test
    fun givenAirtelWithdrawnMessage_whenParse_thenOutboundFromAgent() {
        val payment = parser.parse(withdrawn, CurrencyCode.KES)!!

        assertEquals(PaymentDirection.OUT, payment.direction)
        assertEquals(2_000L, payment.amount.minorUnits)
        assertEquals("GITHURAI AGENT", payment.senderName)
        assertEquals("254731234569", payment.senderPhone)
    }

    // ── negative ─────────────────────────────────────────────────────────────

    @Test
    fun givenUnrelatedText_whenParse_thenNull() {
        assertNull(parser.parse("Hello, this is not a payment", CurrencyCode.KES))
    }
}
