package com.flowgroup.flowta.domain.reconciliation.statement

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.ParsedPayment
import com.flowgroup.flowta.domain.reconciliation.StatementParser
import com.flowgroup.flowta.domain.reconciliation.parser.nairobiInstant
import com.flowgroup.flowta.domain.reconciliation.parser.parseKesAmount
import kotlinx.datetime.Instant
import javax.inject.Inject

/**
 * Parses a Safaricom M-PESA statement exported to CSV. The table has columns like:
 * "Receipt No., Completion Time, Details, Transaction Status, Paid In, Withdrawn, Balance".
 *
 * Only rows with a positive "Paid In" are returned — those are money received (potential sales).
 * Withdrawals, charges and reversals (blank/zero Paid In) are ignored.
 */
class MpesaStatementCsvParser @Inject constructor() : StatementParser {

    override val provider = MobileMoneyProvider.MPESA

    override fun canParse(header: List<String>): Boolean {
        val cols = header.map { it.lowercase() }
        return cols.any { "receipt" in it } && cols.any { "paid in" in it }
    }

    override fun parse(
        header: List<String>,
        dataRows: List<List<String>>,
        currency: CurrencyCode,
    ): List<ParsedPayment> {
        val cols = header.map { it.lowercase() }
        val receiptIdx = cols.indexOfFirst { "receipt" in it }
        val paidInIdx = cols.indexOfFirst { "paid in" in it }
        val detailsIdx = cols.indexOfFirst { "details" in it }
        val dateIdx = cols.indexOfFirst { "completion" in it || "date" in it || "time" in it }
        if (receiptIdx < 0 || paidInIdx < 0) return emptyList()

        return dataRows.mapNotNull { row ->
            val paidIn = row.getOrNull(paidInIdx).orEmpty()
            val amountMinor = parseKesAmount(paidIn)?.takeIf { it > 0L } ?: return@mapNotNull null
            val reference = row.getOrNull(receiptIdx)?.trim()?.takeIf { it.isNotEmpty() }
                ?: return@mapNotNull null
            val details = detailsIdx.takeIf { it >= 0 }?.let { row.getOrNull(it) }.orEmpty()
            val sender = SENDER_REGEX.find(details)

            ParsedPayment(
                provider = provider,
                amount = Money(amountMinor, currency),
                reference = reference,
                senderName = sender?.groupValues?.get(1)?.trim()?.takeIf { it.isNotEmpty() },
                senderPhone = sender?.groupValues?.get(2)?.takeIf { it.isNotEmpty() },
                occurredAt = dateIdx.takeIf { it >= 0 }?.let { row.getOrNull(it) }?.let(::parseStatementDate),
                rawMessage = details.ifBlank { row.joinToString(",") },
            )
        }
    }

    private companion object {
        // "received from JOHN DOE 254712345678" / "Payment from JANE 0712345678"
        val SENDER_REGEX = Regex(
            """from\s+([A-Za-z .'-]+?)\s+(\d{9,12})""",
            RegexOption.IGNORE_CASE,
        )
        // "2026-05-24 13:15:00" or "2026-05-24T13:15"
        val ISO_DATE = Regex("""(\d{4})-(\d{1,2})-(\d{1,2})[ T](\d{1,2}):(\d{2})""")
        // "24/05/2026 1:15 PM"
        val SLASH_DATE = Regex("""(\d{1,2})/(\d{1,2})/(\d{2,4})\s+(\d{1,2}):(\d{2})\s*([AP]M)?""", RegexOption.IGNORE_CASE)

        fun parseStatementDate(text: String): Instant? {
            ISO_DATE.find(text)?.let { m ->
                return nairobiInstant(
                    day = m.groupValues[3].toInt(),
                    month = m.groupValues[2].toInt(),
                    year = m.groupValues[1].toInt(),
                    hour = m.groupValues[4].toInt(),
                    minute = m.groupValues[5].toInt(),
                    meridiem = null,
                )
            }
            SLASH_DATE.find(text)?.let { m ->
                return nairobiInstant(
                    day = m.groupValues[1].toInt(),
                    month = m.groupValues[2].toInt(),
                    year = m.groupValues[3].toInt(),
                    hour = m.groupValues[4].toInt(),
                    minute = m.groupValues[5].toInt(),
                    meridiem = m.groupValues[6].takeIf { it.isNotEmpty() },
                )
            }
            return null
        }
    }
}
