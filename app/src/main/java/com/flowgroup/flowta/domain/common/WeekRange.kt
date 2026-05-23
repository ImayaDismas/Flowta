package com.flowgroup.flowta.domain.common

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * A half-open instant range `[start, endExclusive)`. Used to bound database queries to an
 * ISO calendar week (Monday 00:00 → next Monday 00:00) in a specific [TimeZone].
 */
data class WeekRange(val start: Instant, val endExclusive: Instant) {
    companion object {
        fun thisWeek(now: Instant, zone: TimeZone): WeekRange {
            val today = now.toLocalDateTime(zone).date
            val daysSinceMonday = today.dayOfWeek.isoDayNumber - 1
            val mondayDate = today.minus(DatePeriod(days = daysSinceMonday))
            val nextMondayDate = mondayDate.plus(DatePeriod(days = 7))
            return WeekRange(
                start = mondayDate.atStartOfDayIn(zone),
                endExclusive = nextMondayDate.atStartOfDayIn(zone),
            )
        }

        fun priorWeek(now: Instant, zone: TimeZone): WeekRange {
            val current = thisWeek(now, zone)
            val currentMondayDate = current.start.toLocalDateTime(zone).date
            val priorMondayDate = currentMondayDate.minus(DatePeriod(days = 7))
            return WeekRange(
                start = priorMondayDate.atStartOfDayIn(zone),
                endExclusive = current.start,
            )
        }
    }
}
