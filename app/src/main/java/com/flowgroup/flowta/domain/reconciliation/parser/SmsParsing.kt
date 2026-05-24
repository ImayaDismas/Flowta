package com.flowgroup.flowta.domain.reconciliation.parser

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/** Kenyan mobile-money SMS timestamps are written in local Nairobi time. */
internal val NAIROBI: TimeZone = TimeZone.of("Africa/Nairobi")

/**
 * Parses a KES amount as written in a message ("2,500.00", "500", "1,200.50") into the integer
 * value the app stores in `Money.minorUnits`.
 *
 * IMPORTANT: Flowta models KES in *whole shillings* (no cents) — the rest of the app stores the
 * raw typed shillings in `minorUnits` and never divides by 100. So "Ksh2,500.00" must become 2500,
 * not 250000, or a parsed payment would be 100× too large and never match a recorded sale.
 * Fractional cents are rounded half-up to the nearest shilling.
 *
 * The caller extracts the *received* amount in context — providers also quote a running balance,
 * so a naive whole-message scan would grab the wrong number.
 */
internal fun parseKesAmount(amountText: String): Long? {
    val cleaned = amountText.replace(",", "").trim()
    if (cleaned.isEmpty()) return null
    val parts = cleaned.split(".")
    if (parts.size > 2) return null
    val shillings = parts[0].toLongOrNull()?.takeIf { it >= 0 } ?: return null
    if (parts.size == 1) return shillings
    val cents = parts[1].padEnd(2, '0').take(2).toLongOrNull() ?: return null
    return shillings + if (cents >= 50) 1 else 0
}

/**
 * Builds an [Instant] from SMS date/time components interpreted in Nairobi time.
 * Two-digit years are treated as 2000-based. Pass [meridiem] = null for 24-hour input.
 * Returns null when the components do not form a valid date/time.
 */
internal fun nairobiInstant(
    day: Int,
    month: Int,
    year: Int,
    hour: Int,
    minute: Int,
    meridiem: String?,
): Instant? {
    val fullYear = if (year < 100) 2000 + year else year
    val hour24 = when {
        meridiem == null -> hour
        meridiem.equals("PM", ignoreCase = true) -> if (hour == 12) 12 else hour + 12
        else -> if (hour == 12) 0 else hour
    }
    return runCatching {
        LocalDateTime(fullYear, month, day, hour24, minute).toInstant(NAIROBI)
    }.getOrNull()
}
