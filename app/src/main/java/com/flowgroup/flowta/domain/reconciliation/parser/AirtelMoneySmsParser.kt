package com.flowgroup.flowta.domain.reconciliation.parser

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.ParsedPayment
import com.flowgroup.flowta.domain.model.PaymentDirection
import com.flowgroup.flowta.domain.reconciliation.PaymentSmsParser
import javax.inject.Inject

/**
 * Parses Airtel Money confirmations in both directions:
 *
 * IN  — "TID PP240524.1315.A12345 Confirmed. You have received KES 500.00 from JOHN OTIENO
 *         254731234567 on 24/05/2026 at 01:15 PM. Your Airtel Money balance is KES 1,000.00."
 *
 * OUT — "TID PP240524.1315.A12345 Confirmed. You have sent KES 500.00 to JOHN OTIENO
 *         254731234567 on 24/05/2026 at 01:15 PM. Your Airtel Money balance is KES 500.00."
 *       "TID PP240524.1315.A12345 Confirmed. You have paid KES 200.00 to NAIVAS LTD
 *         on 24/05/2026 at 02:00 PM. Your Airtel Money balance is KES 300.00."
 *       "TID PP240524.1315.A12345 Confirmed. You have withdrawn KES 1,000.00 from AGENT NAME
 *         254731234567 on 24/05/2026 at 03:00 PM. Your Airtel Money balance is KES 200.00."
 *
 * IN maps to a SALE during reconciliation; OUT maps to an EXPENSE. For OUT, the captured
 * name/phone is the counterparty (recipient/merchant/agent).
 */
class AirtelMoneySmsParser @Inject constructor() : PaymentSmsParser {

    override val provider = MobileMoneyProvider.AIRTEL_MONEY

    override fun canParse(raw: String): Boolean {
        val text = raw.uppercase()
        val isAirtel = "AIRTEL" in text
        val hasFlow = "RECEIVED" in text || "SENT" in text || "PAID" in text || "WITHDRAWN" in text
        return isAirtel && hasFlow
    }

    override fun parse(raw: String, currency: CurrencyCode): ParsedPayment? {
        val reference = REFERENCE_REGEX.find(raw)?.groupValues?.get(1)?.takeIf { it.isNotEmpty() }
            ?: return null
        val hit = extract(raw) ?: return null
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
            amount = Money(hit.amountMinor, currency),
            reference = reference,
            senderName = hit.name,
            senderPhone = hit.phone,
            occurredAt = occurredAt,
            rawMessage = raw,
            direction = hit.direction,
        )
    }

    private data class Extracted(
        val amountMinor: Long,
        val name: String?,
        val phone: String?,
        val direction: PaymentDirection,
    )

    private fun extract(raw: String): Extracted? {
        RECEIVED_REGEX.find(raw)?.let { return it.toExtracted(PaymentDirection.IN) }
        SENT_REGEX.find(raw)?.let { return it.toExtracted(PaymentDirection.OUT) }
        PAID_REGEX.find(raw)?.let { return it.toExtracted(PaymentDirection.OUT) }
        WITHDRAWN_REGEX.find(raw)?.let { return it.toExtracted(PaymentDirection.OUT) }
        return null
    }

    /** Groups: 1 = amount, 2 = name, 3 = phone (optional). */
    private fun MatchResult.toExtracted(direction: PaymentDirection): Extracted? {
        val amountMinor = parseKesAmount(groupValues[1]) ?: return null
        return Extracted(
            amountMinor = amountMinor,
            name = groupValues[2].trim().takeIf { it.isNotEmpty() },
            phone = groupValues.getOrNull(3)?.trim()?.takeIf { it.isNotEmpty() },
            direction = direction,
        )
    }

    private companion object {
        // "TID PP240524.1315.A12345" or "Trans ID: PP240524.1315.A12345"
        val REFERENCE_REGEX = Regex(
            """(?:TID|Trans\s*ID)[:\s]+([A-Z0-9.]{6,})""",
            RegexOption.IGNORE_CASE,
        )

        // IN: "received KES 500.00 from JOHN OTIENO 254731234567 on …"
        val RECEIVED_REGEX = Regex(
            """received\s+KES\s*([\d,]+(?:\.\d{1,2})?)\s+from\s+([A-Za-z .'-]+?)(?:\s+(\d{9,12}))?\s+on\b""",
            RegexOption.IGNORE_CASE,
        )

        // OUT: "sent KES 500.00 to JOHN OTIENO 254731234567 on …"
        val SENT_REGEX = Regex(
            """sent\s+KES\s*([\d,]+(?:\.\d{1,2})?)\s+to\s+([A-Za-z .'-]+?)(?:\s+(\d{9,12}))?\s+on\b""",
            RegexOption.IGNORE_CASE,
        )

        // OUT: "paid KES 200.00 to NAIVAS LTD on …" (merchant/till, no phone)
        val PAID_REGEX = Regex(
            """paid\s+KES\s*([\d,]+(?:\.\d{1,2})?)\s+to\s+([A-Za-z .'-]+?)\.?\s+on\b""",
            RegexOption.IGNORE_CASE,
        )

        // OUT: "withdrawn KES 1,000.00 from AGENT NAME 254731234567 on …"
        val WITHDRAWN_REGEX = Regex(
            """withdrawn\s+KES\s*([\d,]+(?:\.\d{1,2})?)\s+from\s+([A-Za-z0-9 .'-]+?)(?:\s+(\d{9,12}))?\s+on\b""",
            RegexOption.IGNORE_CASE,
        )

        // "on 24/05/2026 at 01:15 PM" — meridiem optional (Airtel sometimes uses 24-hour time)
        val TIMESTAMP_REGEX = Regex(
            """on\s+(\d{1,2})/(\d{1,2})/(\d{2,4})\s+at\s+(\d{1,2}):(\d{2})\s*([AP]M)?""",
            RegexOption.IGNORE_CASE,
        )
    }
}
