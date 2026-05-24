package com.flowgroup.flowta.domain.reconciliation.parser

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.ParsedPayment
import com.flowgroup.flowta.domain.model.PaymentDirection
import com.flowgroup.flowta.domain.reconciliation.PaymentSmsParser
import javax.inject.Inject

/**
 * Parses Safaricom M-PESA confirmations in both directions:
 *
 * IN  — "TIX4A2B9P Confirmed. You have received Ksh2,500.00 from MARY WANJIKU 254712345678 on …"
 * OUT — "TIX4A2B9P Confirmed. Ksh500.00 sent to JOHN DOE 254712345678 on …"      (send / paybill)
 *       "TIX4A2B9P Confirmed. Ksh450.00 paid to NAIVAS LTD. on …"                 (buy goods / till)
 *       "TIX4A2B9P Confirmed. … Withdraw Ksh2,000.00 from 123456 - AGENT on …"    (agent withdrawal)
 *
 * IN maps to a SALE during reconciliation; OUT maps to an EXPENSE. For OUT messages the captured
 * name/phone is the counterparty (the recipient), which the rest of the pipeline treats uniformly.
 */
class MpesaSmsParser @Inject constructor() : PaymentSmsParser {

    override val provider = MobileMoneyProvider.MPESA

    override fun canParse(raw: String): Boolean {
        val text = raw.uppercase()
        val isMpesa = "M-PESA" in text || "MPESA" in text
        val hasFlow = "RECEIVED" in text || "SENT TO" in text || "PAID TO" in text ||
            "TRANSFERRED TO" in text || "WITHDRAW" in text
        return isMpesa && hasFlow
    }

    override fun parse(raw: String, currency: CurrencyCode): ParsedPayment? {
        val reference = REFERENCE_REGEX.find(raw)?.groupValues?.get(1) ?: return null
        val hit = extract(raw) ?: return null
        val occurredAt = TIMESTAMP_REGEX.find(raw)?.let { m ->
            nairobiInstant(
                day = m.groupValues[1].toInt(),
                month = m.groupValues[2].toInt(),
                year = m.groupValues[3].toInt(),
                hour = m.groupValues[4].toInt(),
                minute = m.groupValues[5].toInt(),
                meridiem = m.groupValues[6],
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
        WITHDRAW_REGEX.find(raw)?.let { match ->
            val amountMinor = parseKesAmount(match.groupValues[1]) ?: return null
            return Extracted(
                amountMinor = amountMinor,
                name = match.groupValues[2].trim().takeIf { it.isNotEmpty() },
                phone = null,
                direction = PaymentDirection.OUT,
            )
        }
        return null
    }

    /** For the received/sent/paid regexes: group 1 = amount, 2 = name, 3 = phone (optional). */
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
        // Transaction code at the very start of the message, e.g. "TIX4A2B9P Confirmed."
        val REFERENCE_REGEX = Regex("""^\s*([A-Z0-9]{8,12})\b""")

        // "received Ksh2,500.00 from MARY WANJIKU 254712345678 on …" — phone optional (bank receipts
        // omit it); the trailing " on" anchors the lazy name.
        val RECEIVED_REGEX = Regex(
            """received\s+Ksh\s*([\d,]+(?:\.\d{1,2})?)\s+from\s+([A-Za-z .'-]+?)(?:\s+(\d{9,12}))?\s+on\b""",
            RegexOption.IGNORE_CASE,
        )

        // "Ksh500.00 sent to JOHN DOE 254712345678 on …" — also paybill ("… for account 1234")
        // and bank transfers ("transferred to …"). Phone and the "for account" clause are optional.
        val SENT_REGEX = Regex(
            """Ksh\s*([\d,]+(?:\.\d{1,2})?)\s+(?:sent|transferred)\s+to\s+""" +
                """([A-Za-z .'-]+?)(?:\s+(\d{9,12}))?(?:\s+for\s+account\b.*?)?\s+on\b""",
            RegexOption.IGNORE_CASE,
        )

        // "Ksh450.00 paid to NAIVAS LTD. on …" (Buy Goods / till) — no phone.
        val PAID_REGEX = Regex(
            """Ksh\s*([\d,]+(?:\.\d{1,2})?)\s+paid\s+to\s+([A-Za-z .'-]+?)\.?\s+on\b""",
            RegexOption.IGNORE_CASE,
        )

        // "Withdraw Ksh2,000.00 from 123456 - GITHURAI AGENT New M-PESA balance is …" — the agent
        // line carries digits and dashes, so the name is bounded by the trailing balance phrase.
        val WITHDRAW_REGEX = Regex(
            """Withdraw\s+Ksh\s*([\d,]+(?:\.\d{1,2})?)\s+from\s+(.+?)\s+New\s+M-?PESA\b""",
            RegexOption.IGNORE_CASE,
        )

        // "on 24/5/26 at 1:15 PM"
        val TIMESTAMP_REGEX = Regex(
            """on\s+(\d{1,2})/(\d{1,2})/(\d{2,4})\s+at\s+(\d{1,2}):(\d{2})\s*([AP]M)""",
            RegexOption.IGNORE_CASE,
        )
    }
}
