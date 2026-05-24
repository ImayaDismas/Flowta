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

class TkashSmsParserTest {

    private val parser = TkashSmsParser()

    private val received =
        "You have received Ksh500.00 from JANE DOE 254771234567 on 24/05/2026 at 1:15 PM. " +
            "T-Kash Ref: ABC123XY. Your T-Kash balance is Ksh800.00."

    private val sent =
        "You have sent Ksh750.00 to PETER KAMAU 254771234568 on 24/05/2026 at 2:30 PM. " +
            "T-Kash Ref: DEF456ZZ. Your T-Kash balance is Ksh50.00."

    private val paid =
        "You have paid Ksh300.00 to QUICKMART LTD on 24/05/2026 at 3:00 PM. " +
            "T-Kash Ref: XYZ789AB. Your T-Kash balance is Ksh200.00."

    private val withdrawn =
        "You have withdrawn Ksh1,500.00 from KASARANI AGENT 254771234569 on 24/05/2026 at 4:00 PM. " +
            "T-Kash Ref: WER123QQ. Your T-Kash balance is Ksh100.00."

    // ── canParse ──────────────────────────────────────────────────────────────

    @Test
    fun givenTkashReceivedMessage_whenCanParse_thenTrue() {
        assertTrue(parser.canParse(received))
    }

    @Test
    fun givenTkashSentMessage_whenCanParse_thenTrue() {
        assertTrue(parser.canParse(sent))
    }

    @Test
    fun givenTkashPaidMessage_whenCanParse_thenTrue() {
        assertTrue(parser.canParse(paid))
    }

    @Test
    fun givenTkashWithdrawnMessage_whenCanParse_thenTrue() {
        assertTrue(parser.canParse(withdrawn))
    }

    // ── inbound (IN) ──────────────────────────────────────────────────────────

    @Test
    fun givenTkashReceivedMessage_whenParse_thenExtractsAllFields() {
        val payment = parser.parse(received, CurrencyCode.KES)!!

        assertEquals(MobileMoneyProvider.TKASH, payment.provider)
        assertEquals(500L, payment.amount.minorUnits)
        assertEquals("ABC123XY", payment.reference)
        assertEquals("JANE DOE", payment.senderName)
        assertEquals("254771234567", payment.senderPhone)
        assertEquals(PaymentDirection.IN, payment.direction)

        val expected = LocalDateTime(2026, 5, 24, 13, 15)
            .toInstant(TimeZone.of("Africa/Nairobi"))
        assertEquals(expected, payment.occurredAt)
    }

    // ── outbound (OUT) — send to person ───────────────────────────────────────

    @Test
    fun givenTkashSentMessage_whenParse_thenOutboundWithRecipient() {
        val payment = parser.parse(sent, CurrencyCode.KES)!!

        assertEquals(PaymentDirection.OUT, payment.direction)
        assertEquals(750L, payment.amount.minorUnits)
        assertEquals("PETER KAMAU", payment.senderName)
        assertEquals("254771234568", payment.senderPhone)
        assertEquals("DEF456ZZ", payment.reference)
    }

    // ── outbound (OUT) — pay merchant ─────────────────────────────────────────

    @Test
    fun givenTkashPaidMessage_whenParse_thenOutboundToMerchant() {
        val payment = parser.parse(paid, CurrencyCode.KES)!!

        assertEquals(PaymentDirection.OUT, payment.direction)
        assertEquals(300L, payment.amount.minorUnits)
        assertEquals("QUICKMART LTD", payment.senderName)
        assertNull(payment.senderPhone)
    }

    // ── outbound (OUT) — cash withdrawal ──────────────────────────────────────

    @Test
    fun givenTkashWithdrawnMessage_whenParse_thenOutboundFromAgent() {
        val payment = parser.parse(withdrawn, CurrencyCode.KES)!!

        assertEquals(PaymentDirection.OUT, payment.direction)
        assertEquals(1_500L, payment.amount.minorUnits)
        assertEquals("KASARANI AGENT", payment.senderName)
        assertEquals("254771234569", payment.senderPhone)
    }

    // ── negative ─────────────────────────────────────────────────────────────

    @Test
    fun givenUnrelatedText_whenParse_thenNull() {
        assertNull(parser.parse("Hello, this is not a payment", CurrencyCode.KES))
    }
}
