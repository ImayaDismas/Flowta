package com.flowgroup.flowta.domain.usecase.transaction

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CsvExport
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * Serializes the current business's transactions to CSV ("your data is always yours").
 *
 * Pure domain: returns the file content; the UI layer writes it to a user-chosen location.
 * Amounts are emitted as whole shillings (the unit the app stores in Money.minorUnits).
 */
class ExportTransactionsCsvUseCase @Inject constructor(
    private val observeHistory: ObserveHistoryForCurrentBusinessUseCase,
) {
    suspend operator fun invoke(): Result<CsvExport> {
        val items = when (val r = observeHistory().first()) {
            is Result.Success -> r.data
            is Result.Error -> return r
        }

        val sb = StringBuilder()
        sb.append(HEADER.joinToString(",") { escapeCsv(it) }).append("\r\n")
        for (item in items) {
            val t = item.transaction
            val dt = t.occurredAt.toLocalDateTime(TimeZone.currentSystemDefault())
            val date = "${pad(dt.year, 4)}-${pad(dt.monthNumber, 2)}-${pad(dt.dayOfMonth, 2)}"
            val time = "${pad(dt.hour, 2)}:${pad(dt.minute, 2)}"
            val row = listOf(
                date,
                time,
                t.type.name,
                t.amount.minorUnits.toString(),
                t.amount.currency.iso4217,
                item.walletName,
                t.note.orEmpty(),
            )
            sb.append(row.joinToString(",") { escapeCsv(it) }).append("\r\n")
        }

        return Result.Success(CsvExport(content = sb.toString(), rowCount = items.size))
    }

    private companion object {
        val HEADER = listOf("Date", "Time", "Type", "Amount", "Currency", "Wallet", "Note")

        fun pad(value: Int, width: Int): String = value.toString().padStart(width, '0')

        fun escapeCsv(field: String): String =
            if (field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
                "\"" + field.replace("\"", "\"\"") + "\""
            } else {
                field
            }
    }
}
