package com.flowgroup.flowta.domain.model

/**
 * Money is always stored as a minor-unit integer (e.g. KES cents) paired with a currency code.
 * This avoids floating-point error in all arithmetic. Display formatting happens at the UI layer.
 */
data class Money(
    val minorUnits: Long,
    val currency: CurrencyCode,
) {
    init {
        require(minorUnits >= 0) { "Money must be non-negative, got: $minorUnits" }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Cannot add ${currency.iso4217} and ${other.currency.iso4217}" }
        return Money(minorUnits + other.minorUnits, currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Cannot subtract ${other.currency.iso4217} from ${currency.iso4217}" }
        return Money(minorUnits - other.minorUnits, currency)
    }

    companion object {
        fun zero(currency: CurrencyCode): Money = Money(0L, currency)
    }
}
