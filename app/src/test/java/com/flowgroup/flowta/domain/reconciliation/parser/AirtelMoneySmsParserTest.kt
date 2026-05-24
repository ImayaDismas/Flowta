package com.flowgroup.flowta.domain.reconciliation.parser

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AirtelMoneySmsParserTest {

    private val parser = AirtelMoneySmsParser()

    private val received =
        "TID PP240524.1315.A12345 Confirmed. You have received KES 500.00 from JOHN OTIENO " +
            "254731234567 on 24/05/2026 at 01:15 PM. Your Airtel Money balance is KES 1,000.00."

    @Test
    fun givenAirtelReceivedMessage_whenCanParse_thenTrue() {
        assertTrue(parser.canParse(received))
    }

    @Test
    fun givenAirtelReceivedMessage_whenParse_thenExtractsAllFields() {
        val payment = parser.parse(received, CurrencyCode.KES)!!

        assertEquals(MobileMoneyProvider.AIRTEL_MONEY, payment.provider)
        assertEquals(500L, payment.amount.minorUnits)
        assertEquals("PP240524.1315.A12345", payment.reference)
        assertEquals("JOHN OTIENO", payment.senderName)
        assertEquals("254731234567", payment.senderPhone)

        val expected = LocalDateTime(2026, 5, 24, 13, 15)
            .toInstant(TimeZone.of("Africa/Nairobi"))
        assertEquals(expected, payment.occurredAt)
    }
}
