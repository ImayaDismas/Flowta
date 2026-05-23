package com.flowgroup.flowta.ui.screen.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.TransactionTotals
import com.flowgroup.flowta.domain.model.WalletDetail
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.ui.components.EmptyState
import com.flowgroup.flowta.ui.components.TransactionListItem
import com.flowgroup.flowta.ui.state.wallet.WalletDetailUiState
import com.flowgroup.flowta.ui.theme.MoneyIn
import com.flowgroup.flowta.ui.theme.MoneyOut
import com.flowgroup.flowta.ui.viewmodel.wallet.WalletDetailViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun WalletDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onRecordTransaction: () -> Unit,
    onViewAllHistory: () -> Unit,
    viewModel: WalletDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WalletDetailContent(
        uiState = uiState,
        onBack = onBack,
        onEdit = { onEdit(viewModel.walletId) },
        onRecordTransaction = onRecordTransaction,
        onViewAllHistory = onViewAllHistory,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletDetailContent(
    uiState: WalletDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRecordTransaction: () -> Unit,
    onViewAllHistory: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wallet_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
                actions = {
                    if (uiState is WalletDetailUiState.Content) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.wallet_detail_edit),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (uiState is WalletDetailUiState.Content) {
                FloatingActionButton(onClick = onRecordTransaction) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.wallet_detail_record),
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                WalletDetailUiState.Loading -> Unit
                WalletDetailUiState.NotFound -> EmptyState(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    title = stringResource(R.string.wallet_detail_not_found_title),
                    message = stringResource(R.string.wallet_detail_not_found_message),
                )
                is WalletDetailUiState.Error -> EmptyState(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    title = stringResource(R.string.wallet_detail_not_found_title),
                    message = uiState.message.ifBlank {
                        stringResource(R.string.wallet_detail_not_found_message)
                    },
                )
                is WalletDetailUiState.Content -> WalletDetailBody(
                    detail = uiState.detail,
                    onViewAllHistory = onViewAllHistory,
                )
            }
        }
    }
}

@Composable
private fun WalletDetailBody(
    detail: WalletDetail,
    onViewAllHistory: () -> Unit,
) {
    val wallet = detail.wallet
    val currency = wallet.openingBalance.currency
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item { HeaderCard(detail = detail, currency = currency) }
        item { KpiStrip(totals = detail.weekTotals, currency = currency) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.wallet_detail_recent),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (detail.recentTransactions.isNotEmpty()) {
                    TextButton(onClick = onViewAllHistory) {
                        Text(stringResource(R.string.wallet_detail_view_all))
                    }
                }
            }
        }
        if (detail.recentTransactions.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.wallet_detail_recent_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(detail.recentTransactions, key = { it.transaction.id }) { item ->
                TransactionListItem(item = item)
            }
        }
        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun HeaderCard(detail: WalletDetail, currency: CurrencyCode) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = detail.wallet.type.icon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = detail.wallet.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(
                            R.string.wallet_detail_created_on,
                            formatDate(detail.wallet.createdAt),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.wallet_detail_balance_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatMoney(detail.currentBalanceMinor, currency),
                style = MaterialTheme.typography.headlineMedium,
                color = if (detail.currentBalanceMinor < 0L) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun KpiStrip(totals: TransactionTotals, currency: CurrencyCode) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KpiTile(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Outlined.TrendingUp,
            tint = MoneyIn,
            label = stringResource(R.string.wallet_detail_this_week_in),
            amount = formatMoney(totals.salesMinor, currency),
        )
        KpiTile(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Outlined.TrendingDown,
            tint = MoneyOut,
            label = stringResource(R.string.wallet_detail_this_week_out),
            amount = formatMoney(totals.expensesMinor, currency),
        )
    }
}

@Composable
private fun KpiTile(
    modifier: Modifier,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    label: String,
    amount: String,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = tint)
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun formatMoney(minorUnits: Long, currency: CurrencyCode): String {
    val sign = if (minorUnits < 0L) "-" else ""
    val abs = kotlin.math.abs(minorUnits).toString()
    val withCommas = abs.reversed().chunked(3).joinToString(",").reversed()
    return "$sign${currency.iso4217} $withCommas"
}

private fun formatDate(instant: kotlinx.datetime.Instant): String {
    val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = ldt.dayOfMonth.toString().padStart(2, '0')
    val month = ldt.monthNumber.toString().padStart(2, '0')
    return "$day/$month/${ldt.year}"
}

private fun WalletType.icon(): ImageVector = when (this) {
    WalletType.CASH -> Icons.Outlined.Payments
    WalletType.MPESA, WalletType.AIRTEL_MONEY, WalletType.T_KASH -> Icons.Outlined.PhoneAndroid
    WalletType.BANK -> Icons.Outlined.AccountBalance
    WalletType.OTHER -> Icons.Outlined.AccountBalanceWallet
}
