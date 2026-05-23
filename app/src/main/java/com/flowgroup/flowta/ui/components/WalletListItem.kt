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
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.model.WalletWithBalance

@Composable
fun WalletListItem(
    item: WalletWithBalance,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val wallet = item.wallet
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
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
                    imageVector = wallet.type.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
            ) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = wallet.type.displayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = formatBalance(item),
                style = MaterialTheme.typography.titleMedium,
                color = if (item.currentBalanceMinor < 0L) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun formatBalance(item: WalletWithBalance): String {
    val currency = item.wallet.openingBalance.currency.iso4217
    return "${item.currentBalanceMinor} $currency"
}

private fun WalletType.icon(): ImageVector = when (this) {
    WalletType.CASH -> Icons.Outlined.Payments
    WalletType.MPESA, WalletType.AIRTEL_MONEY, WalletType.T_KASH -> Icons.Outlined.PhoneAndroid
    WalletType.BANK -> Icons.Outlined.AccountBalance
    WalletType.OTHER -> Icons.Outlined.AccountBalanceWallet
}

private fun WalletType.displayName(): String = when (this) {
    WalletType.CASH -> "Cash"
    WalletType.MPESA -> "M-Pesa"
    WalletType.AIRTEL_MONEY -> "Airtel Money"
    WalletType.T_KASH -> "T-Kash"
    WalletType.BANK -> "Bank"
    WalletType.OTHER -> "Other"
}
