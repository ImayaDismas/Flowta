package com.flowgroup.flowta.domain.common

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test

class WeekRangeTest {

    private val utc = TimeZone.UTC

    @Test
    fun givenMidweekInstant_whenThisWeek_thenSpansMondayToNextMonday() {
        val wednesday = LocalDateTime(2026, 5, 13, 10, 0).toInstant(utc)

        val range = WeekRange.thisWeek(wednesday, utc)

        assertEquals(LocalDateTime(2026, 5, 11, 0, 0).toInstant(utc), range.start)
        assertEquals(LocalDateTime(2026, 5, 18, 0, 0).toInstant(utc), range.endExclusive)
    }

    @Test
    fun givenMondayMidnightExactly_whenThisWeek_thenStartIsThatMoment() {
        val mondayMidnight = LocalDateTime(2026, 5, 11, 0, 0).toInstant(utc)

        val range = WeekRange.thisWeek(mondayMidnight, utc)

        assertEquals(mondayMidnight, range.start)
        assertEquals(LocalDateTime(2026, 5, 18, 0, 0).toInstant(utc), range.endExclusive)
    }

    @Test
    fun givenSundayJustBeforeMidnight_whenThisWeek_thenStillSameWeek() {
        val sundayLate = LocalDateTime(2026, 5, 17, 23, 59, 59).toInstant(utc)

        val range = WeekRange.thisWeek(sundayLate, utc)

        assertEquals(LocalDateTime(2026, 5, 11, 0, 0).toInstant(utc), range.start)
        assertEquals(LocalDateTime(2026, 5, 18, 0, 0).toInstant(utc), range.endExclusive)
    }

    @Test
    fun givenMidweekInstant_whenPriorWeek_thenSpansSevenDaysEarlier() {
        val wednesday = LocalDateTime(2026, 5, 13, 10, 0).toInstant(utc)

        val range = WeekRange.priorWeek(wednesday, utc)

        assertEquals(LocalDateTime(2026, 5, 4, 0, 0).toInstant(utc), range.start)
        assertEquals(LocalDateTime(2026, 5, 11, 0, 0).toInstant(utc), range.endExclusive)
    }

    @Test
    fun givenYearBoundary_whenThisWeek_thenWeekSpansAcrossNewYear() {
        // 2026-01-01 is a Thursday; ISO week starts Mon 2025-12-29
        val newYearsDay = LocalDateTime(2026, 1, 1, 12, 0).toInstant(utc)

        val range = WeekRange.thisWeek(newYearsDay, utc)

        assertEquals(LocalDateTime(2025, 12, 29, 0, 0).toInstant(utc), range.start)
        assertEquals(LocalDateTime(2026, 1, 5, 0, 0).toInstant(utc), range.endExclusive)
    }
}
