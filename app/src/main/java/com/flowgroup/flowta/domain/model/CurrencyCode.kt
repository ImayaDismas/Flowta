package com.flowgroup.flowta.domain.model

@JvmInline
value class CurrencyCode(val iso4217: String) {
    init {
        require(iso4217.length == 3 && iso4217.all { it.isUpperCase() }) {
            "Currency code must be a 3-letter uppercase ISO 4217 code, got: $iso4217"
        }
    }

    companion object {
        val KES = CurrencyCode("KES")
        val UGX = CurrencyCode("UGX")
        val TZS = CurrencyCode("TZS")
        val RWF = CurrencyCode("RWF")
        val USD = CurrencyCode("USD")
    }
}
