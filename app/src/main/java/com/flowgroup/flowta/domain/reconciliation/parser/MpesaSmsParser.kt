package com.flowgroup.flowta.domain.reconciliation.parser

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.ParsedPayment
import com.flowgroup.flowta.domain.reconciliation.PaymentSmsParser
import javax.inject.Inject

/**
 * Parses Safaricom M-PESA "received" confirmations, e.g.:
 *
 * "TIX4A2B9P Confirmed. You have received Ksh2,500.00 from MARY WANJIKU 254712345678
 *  on 24/5/26 at 1:15 PM. New M-PESA balance is Ksh12,500.00. ..."
 *
 * Only inbound (received) messages are recognised — those are the sales a business reconciles.
 * Outbound "sent to" / "paid to" messages are intentionally ignored.
 */
class MpesaSmsParser @Inject constructor() : PaymentSmsParser {

    override val provider = MobileMoneyProvider.MPESA

    override fun canParse(raw: String): Boolean {
        val text = raw.uppercase()
        return ("M-PESA" in text || "MPESA" in text) && "RECEIVED" in text
    }

    override fun parse(raw: String, currency: CurrencyCode): ParsedPayment? {
        val received = RECEIVED_REGEX.find(raw) ?: return null
        val amountMinor = parseKesAmount(received.groupValues[1]) ?: return null
        val reference = REFERENCE_REGEX.find(raw)?.groupValues?.get(1) ?: return null
        val senderName = received.groupValues[2].trim().takeIf { it.isNotEmpty() }
        val senderPhone = received.groupValues[3].trim().takeIf { it.isNotEmpty() }
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
            amount = Money(amountMinor, currency),
            reference = reference,
            senderName = senderName,
            senderPhone = senderPhone,
            occurredAt = occurredAt,
            rawMessage = raw,
        )
    }

    private companion object {
        // Transaction code at the very start of the message, e.g. "TIX4A2B9P Confirmed."
        val REFERENCE_REGEX = Regex("""^\s*([A-Z0-9]{8,12})\b""")

        // "received Ksh2,500.00 from MARY WANJIKU 254712345678"
        val RECEIVED_REGEX = Regex(
            """received\s+Ksh\s*([\d,]+(?:\.\d{1,2})?)\s+from\s+([A-Za-z .'-]+?)\s+(\d{9,12})""",
            RegexOption.IGNORE_CASE,
        )

        // "on 24/5/26 at 1:15 PM"
        val TIMESTAMP_REGEX = Regex(
            """on\s+(\d{1,2})/(\d{1,2})/(\d{2,4})\s+at\s+(\d{1,2}):(\d{2})\s*([AP]M)""",
            RegexOption.IGNORE_CASE,
        )
    }
}
