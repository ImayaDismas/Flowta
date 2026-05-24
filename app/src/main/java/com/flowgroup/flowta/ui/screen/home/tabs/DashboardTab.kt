package com.flowgroup.flowta.ui.screen.home.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.domain.model.BusinessHealth
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.ui.components.EmptyState
import com.flowgroup.flowta.ui.components.WalletListItem
import com.flowgroup.flowta.ui.state.home.DashboardTabUiState
import com.flowgroup.flowta.ui.theme.MoneyIn
import com.flowgroup.flowta.ui.theme.MoneyOut
import com.flowgroup.flowta.ui.viewmodel.home.DashboardTabViewModel
import kotlin.math.abs
import kotlin.math.round

@Composable
fun DashboardTab(
    onRecordSale: () -> Unit,
    onRecordExpense: () -> Unit,
    onAddWallet: () -> Unit,
    onSeeAllWallets: () -> Unit,
    onOpenWallet: (String) -> Unit,
    onOpenDeni: () -> Unit,
    onOpenReconciliation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardTabViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardTabContent(
        uiState = uiState,
        onRecordSale = onRecordSale,
        onRecordExpense = onRecordExpense,
        onAddWallet = onAddWallet,
        onSeeAllWallets = onSeeAllWallets,
        onOpenWallet = onOpenWallet,
        onOpenDeni = onOpenDeni,
        onOpenReconciliation = onOpenReconciliation,
        modifier = modifier,
    )
}

@Composable
private fun DashboardTabContent(
    uiState: DashboardTabUiState,
    onRecordSale: () -> Unit,
    onRecordExpense: () -> Unit,
    onAddWallet: () -> Unit,
    onSeeAllWallets: () -> Unit,
    onOpenWallet: (String) -> Unit,
    onOpenDeni: () -> Unit,
    onOpenReconciliation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        DashboardTabUiState.Loading -> Unit
        DashboardTabUiState.NoBusiness -> EmptyState(
            icon = Icons.Outlined.ErrorOutline,
            title = stringResource(R.string.dashboard_no_business_title),
            message = stringResource(R.string.dashboard_no_business_message),
            modifier = modifier,
        )
        is DashboardTabUiState.Error -> EmptyState(
            icon = Icons.Outlined.ErrorOutline,
            title = stringResource(R.string.dashboard_error_title),
            message = uiState.message.ifBlank { stringResource(R.string.dashboard_error_message) },
            modifier = modifier,
        )
        is DashboardTabUiState.Content -> DashboardList(
            content = uiState,
            onRecordSale = onRecordSale,
            onRecordExpense = onRecordExpense,
            onAddWallet = onAddWallet,
            onSeeAllWallets = onSeeAllWallets,
            onOpenWallet = onOpenWallet,
            onOpenDeni = onOpenDeni,
            onOpenReconciliation = onOpenReconciliation,
            modifier = modifier,
        )
    }
}

@Composable
private fun DashboardList(
    content: DashboardTabUiState.Content,
    onRecordSale: () -> Unit,
    onRecordExpense: () -> Unit,
    onAddWallet: () -> Unit,
    onSeeAllWallets: () -> Unit,
    onOpenWallet: (String) -> Unit,
    onOpenDeni: () -> Unit,
    onOpenReconciliation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item(key = "quick_actions") {
            QuickActionsSection(
                onRecordSale = onRecordSale,
                onRecordExpense = onRecordExpense,
                onAddWallet = onAddWallet,
            )
        }
        item(key = "business_health") {
            BusinessHealthSection(health = content.health)
        }
        item(key = "deni_owed") {
            DeniOwedCard(
                outstandingMinor = content.outstandingDeniMinor,
                currency = content.currency,
                onClick = onOpenDeni,
            )
        }
        item(key = "reconcile") {
            ReconcileCard(onClick = onOpenReconciliation)
        }
        item(key = "wallets_header") {
            WalletsHeader(
                showSeeAll = content.totalWalletCount > content.walletPreview.size,
                onSeeAll = onSeeAllWallets,
            )
        }
        if (content.walletPreview.isEmpty()) {
            item(key = "wallets_empty") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = stringResource(R.string.wallets_empty_message),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(content.walletPreview, key = { it.wallet.id }) { item ->
                WalletListItem(item = item, onClick = { onOpenWallet(item.wallet.id) })
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onRecordSale: () -> Unit,
    onRecordExpense: () -> Unit,
    onAddWallet: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel(text = stringResource(R.string.dashboard_quick_actions))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionTile(
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                iconTint = MoneyIn,
                label = stringResource(R.string.dashboard_action_record_sale),
                onClick = onRecordSale,
                modifier = Modifier.weight(1f),
            )
            QuickActionTile(
                icon = Icons.AutoMirrored.Outlined.TrendingDown,
                iconTint = MoneyOut,
                label = stringResource(R.string.dashboard_action_record_expense),
                onClick = onRecordExpense,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionTile(
                icon = Icons.Outlined.AccountBalanceWallet,
                iconTint = MaterialTheme.colorScheme.primary,
                label = stringResource(R.string.dashboard_action_new_wallet),
                onClick = onAddWallet,
                modifier = Modifier.weight(1f),
            )
            QuickActionTile(
                icon = Icons.Outlined.Work,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                label = stringResource(R.string.dashboard_action_record_salary),
                onClick = {},
                enabled = false,
                badge = stringResource(R.string.dashboard_action_coming_soon),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    badge: String? = null,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) iconTint else iconTint.copy(alpha = 0.5f),
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (badge != null) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.outline,
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun BusinessHealthSection(health: BusinessHealth) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SectionLabel(text = stringResource(R.string.dashboard_business_health))
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = stringResource(R.string.dashboard_period_this_week),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HealthMetricCard(
                label = stringResource(R.string.dashboard_revenue),
                value = formatMoney(health.revenue.minorUnits, health.revenue.currency),
                deltaPercent = health.revenueDeltaPercent,
                higherIsBetter = true,
                modifier = Modifier.weight(1f),
            )
            HealthMetricCard(
                label = stringResource(R.string.dashboard_expenses),
                value = formatMoney(health.expenses.minorUnits, health.expenses.currency),
                deltaPercent = health.expensesDeltaPercent,
                higherIsBetter = false,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HealthMetricCard(
    label: String,
    value: String,
    deltaPercent: Double?,
    higherIsBetter: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = deltaText(deltaPercent),
                style = MaterialTheme.typography.bodySmall,
                color = deltaColor(deltaPercent, higherIsBetter),
            )
        }
    }
}

@Composable
private fun WalletsHeader(
    showSeeAll: Boolean,
    onSeeAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SectionLabel(text = stringResource(R.string.dashboard_wallets_at_a_glance))
        if (showSeeAll) {
            TextButton(onClick = onSeeAll) {
                Text(text = stringResource(R.string.dashboard_see_all))
            }
        }
    }
}

@Composable
private fun DeniOwedCard(
    outstandingMinor: Long,
    currency: CurrencyCode,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_deni_owed_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatMoney(outstandingMinor, currency),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReconcileCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Sync,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.dashboard_reconcile_label),
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun deltaText(percent: Double?): String {
    if (percent == null) return stringResource(R.string.dashboard_wow_delta_none)
    val rounded = round(abs(percent) * 10.0) / 10.0
    val sign = if (percent >= 0) "+" else "-"
    val formatted = if (rounded % 1.0 == 0.0) {
        "${sign}${rounded.toInt()}%"
    } else {
        "${sign}${rounded}%"
    }
    return stringResource(R.string.dashboard_wow_delta, formatted)
}

@Composable
private fun deltaColor(percent: Double?, higherIsBetter: Boolean): Color {
    return when {
        percent == null -> MaterialTheme.colorScheme.onSurfaceVariant
        (percent >= 0) == higherIsBetter -> MoneyIn
        else -> MoneyOut
    }
}

private fun formatMoney(minorUnits: Long, currency: CurrencyCode): String {
    val negative = minorUnits < 0
    val abs = if (negative) -minorUnits else minorUnits
    val withCommas = abs.toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
    return if (negative) {
        "-${currency.iso4217} $withCommas"
    } else {
        "${currency.iso4217} $withCommas"
    }
}
