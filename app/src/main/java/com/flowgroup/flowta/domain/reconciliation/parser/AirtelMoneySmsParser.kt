package com.flowgroup.flowta.domain.reconciliation.parser

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.ParsedPayment
import com.flowgroup.flowta.domain.reconciliation.PaymentSmsParser
import javax.inject.Inject

/**
 * Parses Airtel Money "received" confirmations, e.g.:
 *
 * "TID PP240524.1315.A12345 Confirmed. You have received KES 500.00 from JOHN OTIENO 254731234567
 *  on 24/05/2026 at 01:15 PM. Your Airtel Money balance is KES 1,000.00."
 *
 * Airtel formats vary more than M-PESA in the field; this rule is seeded against the canonical
 * receive layout and is meant to be refined as real samples come in (the engine is pluggable
 * precisely so a provider rule can change without touching the rest of the pipeline).
 */
class AirtelMoneySmsParser @Inject constructor() : PaymentSmsParser {

    override val provider = MobileMoneyProvider.AIRTEL_MONEY

    override fun canParse(raw: String): Boolean {
        val text = raw.uppercase()
        return "AIRTEL" in text && "RECEIVED" in text
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
        // "TID PP240524.1315.A12345" or "Trans ID: PP240524.1315.A12345"
        val REFERENCE_REGEX = Regex(
            """(?:TID|Trans\s*ID)[:\s]+([A-Z0-9.]{6,})""",
            RegexOption.IGNORE_CASE,
        )

        // "received KES 500.00 from JOHN OTIENO 254731234567"
        val RECEIVED_REGEX = Regex(
            """received\s+(?:KES|Ksh)\s*([\d,]+(?:\.\d{1,2})?)\s+from\s+([A-Za-z .'-]+?)\s+(\d{9,12})""",
            RegexOption.IGNORE_CASE,
        )

        // "on 24/05/2026 at 01:15 PM" — meridiem optional (Airtel sometimes uses 24-hour time).
        val TIMESTAMP_REGEX = Regex(
            """on\s+(\d{1,2})/(\d{1,2})/(\d{2,4})\s+at\s+(\d{1,2}):(\d{2})\s*([AP]M)?""",
            RegexOption.IGNORE_CASE,
        )
    }
}
