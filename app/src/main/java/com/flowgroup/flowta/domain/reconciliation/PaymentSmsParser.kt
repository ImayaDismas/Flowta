package com.flowgroup.flowta.domain.reconciliation

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.ParsedPayment

/**
 * One rule per mobile-money provider. The [PaymentSmsParserEngine] asks each parser whether it
 * recognises a raw message, then delegates to the first that claims it.
 *
 * Adding a provider is just a new implementation bound `@IntoSet` — no engine change required.
 * This is the pluggable-engine design the product depends on.
 */
interface PaymentSmsParser {
    val provider: MobileMoneyProvider

    /** Cheap check: does this message look like it came from [provider]? */
    fun canParse(raw: String): Boolean

    /** Extracts a payment, or null if the message is recognised but cannot be parsed. */
    fun parse(raw: String, currency: CurrencyCode): ParsedPayment?
}
