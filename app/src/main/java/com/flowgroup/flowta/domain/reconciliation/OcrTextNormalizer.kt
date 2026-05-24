package com.flowgroup.flowta.domain.reconciliation

/**
 * Repairs the digit/letter confusions OCR commonly makes inside money amounts (e.g. "Kshl,250.00"
 * for "Ksh1,250.00"), so OCR'd payment messages parse with the same rules as pasted ones. Only the
 * amount run right after a currency marker is touched, leaving names, references and prose intact.
 */
object OcrTextNormalizer {

    private val AMOUNT_AFTER_CURRENCY = Regex("""(?i)(Ksh|KES)(\s*)([0-9A-Za-z.,]+)""")

    private val CONFUSIONS = mapOf(
        'l' to '1', 'I' to '1', '|' to '1',
        'O' to '0', 'o' to '0',
        'S' to '5', 'B' to '8', 'Z' to '2',
    )

    fun normalize(raw: String): String =
        AMOUNT_AFTER_CURRENCY.replace(raw) { match ->
            match.groupValues[1] + match.groupValues[2] + repairAmount(match.groupValues[3])
        }

    private fun repairAmount(amount: String): String =
        amount.map { CONFUSIONS[it] ?: it }.joinToString("")
}
