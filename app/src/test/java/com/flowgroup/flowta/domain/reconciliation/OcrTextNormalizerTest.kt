package com.flowgroup.flowta.domain.reconciliation

import org.junit.Assert.assertEquals
import org.junit.Test

class OcrTextNormalizerTest {

    @Test
    fun givenLetterForOneInAmount_whenNormalized_thenDigitRestored() {
        assertEquals(
            "You have received Ksh1,250.00 from JOHN OCR",
            OcrTextNormalizer.normalize("You have received Kshl,250.00 from JOHN OCR"),
        )
    }

    @Test
    fun givenCleanAmount_whenNormalized_thenUnchanged() {
        assertEquals(
            "New M-PESA balance is Ksh30,000.00",
            OcrTextNormalizer.normalize("New M-PESA balance is Ksh30,000.00"),
        )
    }

    @Test
    fun givenLetterOhAndEssConfusions_whenNormalized_thenDigitsRestored() {
        assertEquals("KES 500.00", OcrTextNormalizer.normalize("KES S0O.00"))
    }

    @Test
    fun givenNamesAndPhone_whenNormalized_thenLeftIntact() {
        val input = "from MARY WANJIKU 254712345678 on 24/5/26"
        assertEquals(input, OcrTextNormalizer.normalize(input))
    }
}
