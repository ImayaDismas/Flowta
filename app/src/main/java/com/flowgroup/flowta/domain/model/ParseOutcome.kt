package com.flowgroup.flowta.domain.model

/**
 * Result of parsing one or more raw messages.
 *
 * @param submitted messages handed to the parser (after trimming blanks)
 * @param recognized messages a provider rule could read
 * @param stored newly persisted payments (duplicates by reference are not re-stored)
 */
data class ParseOutcome(
    val submitted: Int,
    val recognized: Int,
    val stored: Int,
) {
    val duplicates: Int get() = recognized - stored
    val unrecognized: Int get() = submitted - recognized
}
