package com.flowgroup.flowta.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.TransactionWithWallet
import com.flowgroup.flowta.ui.theme.MoneyIn
import com.flowgroup.flowta.ui.theme.MoneyOut
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TransactionListItem(
    item: TransactionWithWallet,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val isSale = item.transaction.type == TransactionType.SALE
    val accent: Color = if (isSale) MoneyIn else MoneyOut
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isSale) Icons.AutoMirrored.Outlined.TrendingUp
                    else Icons.AutoMirrored.Outlined.TrendingDown,
                    contentDescription = null,
                    tint = accent,
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
            ) {
                Text(
                    text = item.transaction.note?.takeIf { it.isNotBlank() } ?: item.walletName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${item.walletName} · ${formatOccurredAt(item)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = formatAmount(item),
                style = MaterialTheme.typography.titleMedium,
                color = accent,
            )
        }
    }
}

private fun formatAmount(item: TransactionWithWallet): String {
    val sign = if (item.transaction.type == TransactionType.SALE) "+" else "-"
    return "$sign${item.transaction.amount.minorUnits} ${item.transaction.amount.currency.iso4217}"
}

private fun formatOccurredAt(item: TransactionWithWallet): String {
    val ldt = item.transaction.occurredAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = ldt.dayOfMonth.toString().padStart(2, '0')
    val month = ldt.monthNumber.toString().padStart(2, '0')
    val hour = ldt.hour.toString().padStart(2, '0')
    val minute = ldt.minute.toString().padStart(2, '0')
    return "$day/$month $hour:$minute"
}
