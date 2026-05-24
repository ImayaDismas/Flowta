package com.flowgroup.flowta.domain.reconciliation.parser

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.ParsedPayment
import com.flowgroup.flowta.domain.reconciliation.PaymentSmsParser
import javax.inject.Inject

/**
 * Parses Telkom T-Kash "received" confirmations, e.g.:
 *
 * "You have received Ksh500.00 from JANE DOE 254771234567 on 24/05/2026 at 1:15 PM.
 *  T-Kash Ref: ABC123XY. Your T-Kash balance is Ksh800.00."
 *
 * T-Kash is the least common provider and its formats are the least documented; this rule is
 * seeded against the canonical receive layout and refined as samples arrive (pluggable engine).
 */
class TkashSmsParser @Inject constructor() : PaymentSmsParser {

    override val provider = MobileMoneyProvider.TKASH

    override fun canParse(raw: String): Boolean {
        val text = raw.uppercase()
        return ("T-KASH" in text || "TKASH" in text) && "RECEIVED" in text
    }

    override fun parse(raw: String, currency: CurrencyCode): ParsedPayment? {
        val received = RECEIVED_REGEX.find(raw) ?: return null
        val amountMinor = parseKesAmount(received.groupValues[1]) ?: return null
        val reference = REFERENCE_REGEX.find(raw)?.groupValues?.get(1)?.takeIf { it.isNotEmpty() }
            ?: return null
        val senderName = received.groupValues[2].trim().takeIf { it.isNotEmpty() }
        val senderPhone = received.groupValues[3].trim().takeIf { it.isNotEmpty() }
        val occurredAt = TIMESTAMP_REGEX.find(raw)?.let { m ->
            nairobiInstant(
                day = m.groupValues[1].toInt(),
                month = m.groupValues[2].toInt(),
                year = m.groupValues[3].toInt(),
                hour = m.groupValues[4].toInt(),
                minute = m.groupValues[5].toInt(),
                meridiem = m.groupValues[6].takeIf { it.isNotEmpty() },
            )
        }
        return ParsedPayment(
            provider = provider,
            amount = Money(amountMinor, currency),
            reference = reference,
            senderName = senderName,
            senderPhone = senderPhone,
            occurredAt = occurredAt,
            rawMessage = raw,
        )
    }

    private companion object {
        // "T-Kash Ref: ABC123XY" or "Ref ABC123XY"
        val REFERENCE_REGEX = Regex(
            """Ref[:\s]+([A-Z0-9]{4,})""",
            RegexOption.IGNORE_CASE,
        )

        // "received Ksh500.00 from JANE DOE 254771234567"
        val RECEIVED_REGEX = Regex(
            """received\s+(?:KES|Ksh)\s*([\d,]+(?:\.\d{1,2})?)\s+from\s+([A-Za-z .'-]+?)\s+(\d{9,12})""",
            RegexOption.IGNORE_CASE,
        )

        // "on 24/05/2026 at 1:15 PM" — meridiem optional.
        val TIMESTAMP_REGEX = Regex(
            """on\s+(\d{1,2})/(\d{1,2})/(\d{2,4})\s+at\s+(\d{1,2}):(\d{2})\s*([AP]M)?""",
            RegexOption.IGNORE_CASE,
        )
    }
}
