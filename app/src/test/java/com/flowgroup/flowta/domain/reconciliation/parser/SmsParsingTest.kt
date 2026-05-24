package com.flowgroup.flowta.domain.reconciliation.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmsParsingTest {

    @Test
    fun givenAmountWithThousandsAndCents_whenParsing_thenWholeShillings() {
        // App models KES in whole shillings — cents of .00 contribute nothing.
        assertEquals(2_500L, parseKesAmount("2,500.00"))
    }

    @Test
    fun givenWholeAmount_whenParsing_thenSameValue() {
        assertEquals(500L, parseKesAmount("500"))
    }

    @Test
    fun givenCents_whenParsing_thenRoundedHalfUpToShilling() {
        assertEquals(1_200L, parseKesAmount("1,200.49"))
        assertEquals(1_201L, parseKesAmount("1,200.50"))
    }

    @Test
    fun givenNonNumericText_whenParsing_thenNull() {
        assertNull(parseKesAmount("abc"))
        assertNull(parseKesAmount(""))
    }

    @Test
    fun givenTwoDigitYearAndPm_whenBuildingInstant_thenNairobiTime() {
        // 1:15 PM EAT (UTC+3) on 24 May 2026 == 10:15 UTC.
        val instant = nairobiInstant(day = 24, month = 5, year = 26, hour = 1, minute = 15, meridiem = "PM")
        assertEquals("2026-05-24T10:15:00Z", instant.toString())
    }

    @Test
    fun givenMidnightTwelveAm_whenBuildingInstant_thenZeroHour() {
        // 12:30 AM EAT == 21:30 UTC on the previous day.
        val instant = nairobiInstant(day = 24, month = 5, year = 2026, hour = 12, minute = 30, meridiem = "AM")
        assertEquals("2026-05-23T21:30:00Z", instant.toString())
    }

    @Test
    fun givenInvalidDate_whenBuildingInstant_thenNull() {
        assertNull(nairobiInstant(day = 32, month = 13, year = 2026, hour = 1, minute = 0, meridiem = "PM"))
    }
}
