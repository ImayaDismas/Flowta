package com.flowgroup.flowta.domain.reconciliation

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.ParsedPayment

/**
 * One rule per statement format (mirrors [PaymentSmsParser]). The [StatementParserEngine] finds the
 * header row a parser recognises, then hands it the data rows. A new provider/bank CSV is added by
 * implementing this and binding it `@IntoSet` — no engine change.
 */
interface StatementParser {
    val provider: MobileMoneyProvider

    /** Does this row look like the header of a statement this parser understands? */
    fun canParse(header: List<String>): Boolean

    /** Extracts received payments (money in) from the data rows; ignores withdrawals/charges. */
    fun parse(
        header: List<String>,
        dataRows: List<List<String>>,
        currency: CurrencyCode,
    ): List<ParsedPayment>
}
