package com.flowgroup.flowta.domain.reconciliation

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.ParsedPayment
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dispatches a raw mobile-money message to the first provider rule that recognises it.
 * Provider rules are injected as a set, so a new provider is added purely by binding a new
 * [PaymentSmsParser] — the engine and the rest of the reconciliation pipeline stay unchanged.
 */
@Singleton
class PaymentSmsParserEngine @Inject constructor(
    private val parsers: Set<@JvmSuppressWildcards PaymentSmsParser>,
) {
    /** Identifies the provider of [raw], or null if no rule recognises it. */
    fun detectProvider(raw: String): MobileMoneyProvider? {
        val text = raw.trim()
        if (text.isEmpty()) return null
        return parsers.firstOrNull { it.canParse(text) }?.provider
    }

    /** Parses a single message, or null if unrecognised or unparseable. */
    fun parse(raw: String, currency: CurrencyCode): ParsedPayment? {
        val text = raw.trim()
        if (text.isEmpty()) return null
        return parsers.firstOrNull { it.canParse(text) }?.parse(text, currency)
    }

    /** Parses many messages, dropping any that cannot be parsed. */
    fun parseAll(messages: List<String>, currency: CurrencyCode): List<ParsedPayment> =
        messages.mapNotNull { parse(it, currency) }
}
