package com.flowgroup.flowta.domain.reconciliation.parser

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TkashSmsParserTest {

    private val parser = TkashSmsParser()

    private val received =
        "You have received Ksh500.00 from JANE DOE 254771234567 on 24/05/2026 at 1:15 PM. " +
            "T-Kash Ref: ABC123XY. Your T-Kash balance is Ksh800.00."

    @Test
    fun givenTkashReceivedMessage_whenCanParse_thenTrue() {
        assertTrue(parser.canParse(received))
    }

    @Test
    fun givenTkashReceivedMessage_whenParse_thenExtractsAllFields() {
        val payment = parser.parse(received, CurrencyCode.KES)!!

        assertEquals(MobileMoneyProvider.TKASH, payment.provider)
        assertEquals(500L, payment.amount.minorUnits)
        assertEquals("ABC123XY", payment.reference)
        assertEquals("JANE DOE", payment.senderName)
        assertEquals("254771234567", payment.senderPhone)

        val expected = LocalDateTime(2026, 5, 24, 13, 15)
            .toInstant(TimeZone.of("Africa/Nairobi"))
        assertEquals(expected, payment.occurredAt)
    }
}
