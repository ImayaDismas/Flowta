package com.flowgroup.flowta.domain.reconciliation

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.reconciliation.parser.AirtelMoneySmsParser
import com.flowgroup.flowta.domain.reconciliation.parser.MpesaSmsParser
import com.flowgroup.flowta.domain.reconciliation.parser.TkashSmsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaymentSmsParserEngineTest {

    private val engine = PaymentSmsParserEngine(
        setOf(MpesaSmsParser(), AirtelMoneySmsParser(), TkashSmsParser()),
    )

    private val mpesa =
        "TIX4A2B9P Confirmed. You have received Ksh2,500.00 from MARY WANJIKU 254712345678 " +
            "on 24/5/26 at 1:15 PM. New M-PESA balance is Ksh12,500.00."
    private val airtel =
        "TID PP240524.1315.A12345 Confirmed. You have received KES 500.00 from JOHN OTIENO " +
            "254731234567 on 24/05/2026 at 01:15 PM. Your Airtel Money balance is KES 1,000.00."
    private val tkash =
        "You have received Ksh500.00 from JANE DOE 254771234567 on 24/05/2026 at 1:15 PM. " +
            "T-Kash Ref: ABC123XY."

    @Test
    fun givenMessages_whenDetectingProvider_thenRoutesToCorrectRule() {
        assertEquals(MobileMoneyProvider.MPESA, engine.detectProvider(mpesa))
        assertEquals(MobileMoneyProvider.AIRTEL_MONEY, engine.detectProvider(airtel))
        assertEquals(MobileMoneyProvider.TKASH, engine.detectProvider(tkash))
    }

    @Test
    fun givenUnrecognisedOrBlank_whenParsing_thenNull() {
        assertNull(engine.parse("just some text", CurrencyCode.KES))
        assertNull(engine.parse("   ", CurrencyCode.KES))
        assertNull(engine.detectProvider("   "))
    }

    @Test
    fun givenMixedBatch_whenParseAll_thenDropsUnparseable() {
        val parsed = engine.parseAll(listOf(mpesa, "garbage", tkash), CurrencyCode.KES)

        assertEquals(2, parsed.size)
        assertEquals(MobileMoneyProvider.MPESA, parsed[0].provider)
        assertEquals(MobileMoneyProvider.TKASH, parsed[1].provider)
    }
}
