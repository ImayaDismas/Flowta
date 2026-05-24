package com.flowgroup.flowta.domain.reconciliation.parser

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.ParsedPayment
import com.flowgroup.flowta.domain.model.PaymentDirection
import com.flowgroup.flowta.domain.reconciliation.PaymentSmsParser
import javax.inject.Inject

/**
 * Parses Telkom T-Kash confirmations in both directions:
 *
 * IN  — "You have received Ksh500.00 from JANE DOE 254771234567 on 24/05/2026 at 1:15 PM.
 *         T-Kash Ref: ABC123XY. Your T-Kash balance is Ksh800.00."
 *
 * OUT — "You have sent Ksh500.00 to JOHN DOE 254771234567 on 24/05/2026 at 1:15 PM.
 *         T-Kash Ref: ABC123XY. Your T-Kash balance is Ksh300.00."
 *       "You have paid Ksh200.00 to QUICKMART LTD on 24/05/2026 at 2:00 PM.
 *         T-Kash Ref: XYZ789AB. Your T-Kash balance is Ksh100.00."
 *       "You have withdrawn Ksh1,000.00 from AGENT NAME 254771234567 on 24/05/2026 at 3:00 PM.
 *         T-Kash Ref: WER123QQ. Your T-Kash balance is Ksh200.00."
 *
 * IN maps to a SALE during reconciliation; OUT maps to an EXPENSE. For OUT, the captured
 * name/phone is the counterparty (recipient/merchant/agent).
 */
class TkashSmsParser @Inject constructor() : PaymentSmsParser {

    override val provider = MobileMoneyProvider.TKASH

    override fun canParse(raw: String): Boolean {
        val text = raw.uppercase()
        val isTkash = "T-KASH" in text || "TKASH" in text
        val hasFlow = "RECEIVED" in text || "SENT" in text || "PAID" in text || "WITHDRAWN" in text
        return isTkash && hasFlow
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
        // "T-Kash Ref: ABC123XY" or "Ref ABC123XY"
        val REFERENCE_REGEX = Regex(
            """Ref[:\s]+([A-Z0-9]{4,})""",
            RegexOption.IGNORE_CASE,
        )

        // IN: "received Ksh500.00 from JANE DOE 254771234567 on …"
        val RECEIVED_REGEX = Regex(
            """received\s+(?:KES|Ksh)\s*([\d,]+(?:\.\d{1,2})?)\s+from\s+([A-Za-z .'-]+?)(?:\s+(\d{9,12}))?\s+on\b""",
            RegexOption.IGNORE_CASE,
        )

        // OUT: "sent Ksh500.00 to JOHN DOE 254771234567 on …"
        val SENT_REGEX = Regex(
            """sent\s+(?:KES|Ksh)\s*([\d,]+(?:\.\d{1,2})?)\s+to\s+([A-Za-z .'-]+?)(?:\s+(\d{9,12}))?\s+on\b""",
            RegexOption.IGNORE_CASE,
        )

        // OUT: "paid Ksh200.00 to QUICKMART LTD on …" (merchant/till, no phone)
        val PAID_REGEX = Regex(
            """paid\s+(?:KES|Ksh)\s*([\d,]+(?:\.\d{1,2})?)\s+to\s+([A-Za-z .'-]+?)\.?\s+on\b""",
            RegexOption.IGNORE_CASE,
        )

        // OUT: "withdrawn Ksh1,000.00 from AGENT NAME 254771234567 on …"
        val WITHDRAWN_REGEX = Regex(
            """withdrawn\s+(?:KES|Ksh)\s*([\d,]+(?:\.\d{1,2})?)\s+from\s+([A-Za-z0-9 .'-]+?)(?:\s+(\d{9,12}))?\s+on\b""",
            RegexOption.IGNORE_CASE,
        )

        // "on 24/05/2026 at 1:15 PM" — meridiem optional
        val TIMESTAMP_REGEX = Regex(
            """on\s+(\d{1,2})/(\d{1,2})/(\d{2,4})\s+at\s+(\d{1,2}):(\d{2})\s*([AP]M)?""",
            RegexOption.IGNORE_CASE,
        )
    }
}
