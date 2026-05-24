package com.flowgroup.flowta.ui.screen.reconciliation

import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** KES is modelled in whole shillings — render the raw value with thousands separators. */
internal fun formatMoney(minorUnits: Long, currency: CurrencyCode): String {
    val negative = minorUnits < 0L
    val abs = if (negative) -minorUnits else minorUnits
    val grouped = abs.toString().reversed().chunked(3).joinToString(",").reversed()
    return if (negative) "-${currency.iso4217} $grouped" else "${currency.iso4217} $grouped"
}

internal fun MobileMoneyProvider.displayName(): String = when (this) {
    MobileMoneyProvider.MPESA -> "M-Pesa"
    MobileMoneyProvider.AIRTEL_MONEY -> "Airtel Money"
    MobileMoneyProvider.TKASH -> "T-Kash"
}

internal fun formatWhen(instant: Instant): String {
    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val hh = dt.hour.toString().padStart(2, '0')
    val mm = dt.minute.toString().padStart(2, '0')
    return "${dt.dayOfMonth}/${dt.monthNumber} $hh:$mm"
}
