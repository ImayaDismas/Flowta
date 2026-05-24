package com.flowgroup.flowta.ui.screen.reconciliation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.domain.model.ReceivedPayment
import com.flowgroup.flowta.domain.model.ReconciliationSummary
import com.flowgroup.flowta.ui.components.EmptyState
import com.flowgroup.flowta.ui.state.reconciliation.ReconciliationHubUiState
import com.flowgroup.flowta.ui.theme.MoneyIn
import com.flowgroup.flowta.ui.theme.MoneyOut
import com.flowgroup.flowta.ui.viewmodel.reconciliation.ReconciliationHubViewModel

@Composable
fun ReconciliationHubScreen(
    onBack: () -> Unit,
    onPaste: () -> Unit,
    onScan: () -> Unit,
    onImport: () -> Unit,
    onInbox: () -> Unit,
    onOpenPayment: (String) -> Unit,
    viewModel: ReconciliationHubViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReconciliationHubContent(
        uiState = uiState,
        onBack = onBack,
        onPaste = onPaste,
        onScan = onScan,
        onImport = onImport,
        onInbox = onInbox,
        onOpenPayment = onOpenPayment,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReconciliationHubContent(
    uiState: ReconciliationHubUiState,
    onBack: () -> Unit,
    onPaste: () -> Unit,
    onScan: () -> Unit,
    onImport: () -> Unit,
    onInbox: () -> Unit,
    onOpenPayment: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reconciliation_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "subtitle") {
                Text(
                    text = stringResource(R.string.reconciliation_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item(key = "summary") {
                val summary = (uiState as? ReconciliationHubUiState.Content)?.summary
                SummaryCard(summary)
            }
            item(key = "actions") {
                InputMethodRow(onPaste = onPaste, onScan = onScan, onImport = onImport, onInbox = onInbox)
            }

            when (uiState) {
                ReconciliationHubUiState.Loading -> Unit
                ReconciliationHubUiState.Empty -> item(key = "empty") {
                    EmptyState(
                        icon = Icons.Outlined.Sync,
                        title = stringResource(R.string.reconciliation_empty_title),
                        message = stringResource(R.string.reconciliation_empty_message),
                    )
                }
                is ReconciliationHubUiState.Error -> item(key = "error") {
                    EmptyState(
                        icon = Icons.Outlined.Sync,
                        title = stringResource(R.string.reconciliation_empty_title),
                        message = uiState.message.ifBlank {
                            stringResource(R.string.reconciliation_empty_message)
                        },
                    )
                }
                is ReconciliationHubUiState.Content -> {
                    val summary = uiState.summary
                    if (summary.unmatched.isNotEmpty()) {
                        item(key = "unmatched_header") {
                            SectionLabel(stringResource(R.string.reconciliation_section_unmatched))
                        }
                        items(summary.unmatched, key = { it.id }) { payment ->
                            PaymentRow(payment = payment, matched = false) { onOpenPayment(payment.id) }
                        }
                    }
                    if (summary.matched.isNotEmpty()) {
                        item(key = "matched_header") {
                            SectionLabel(stringResource(R.string.reconciliation_section_matched))
                        }
                        items(summary.matched, key = { it.id }) { payment ->
                            PaymentRow(payment = payment, matched = true, onClick = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: ReconciliationSummary?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(
                    R.string.reconciliation_matched_of_total,
                    summary?.matchedCount ?: 0,
                    summary?.totalCount ?: 0,
                ),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatPill(
                    label = stringResource(R.string.reconciliation_matched),
                    value = summary?.matchedCount ?: 0,
                    tint = MoneyIn,
                    modifier = Modifier.weight(1f),
                )
                StatPill(
                    label = stringResource(R.string.reconciliation_unmatched),
                    value = summary?.unmatchedCount ?: 0,
                    tint = MoneyOut,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: Int, tint: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = value.toString(), style = MaterialTheme.typography.titleLarge, color = tint)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InputMethodRow(
    onPaste: () -> Unit,
    onScan: () -> Unit,
    onImport: () -> Unit,
    onInbox: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        InputMethodTile(Icons.Outlined.ContentPaste, stringResource(R.string.reconciliation_action_paste), onPaste, Modifier.weight(1f))
        InputMethodTile(Icons.Outlined.PhotoCamera, stringResource(R.string.reconciliation_action_scan), onScan, Modifier.weight(1f))
        InputMethodTile(Icons.Outlined.FileUpload, stringResource(R.string.reconciliation_action_import), onImport, Modifier.weight(1f))
        InputMethodTile(Icons.Outlined.Inbox, stringResource(R.string.reconciliation_action_inbox), onInbox, Modifier.weight(1f))
    }
}

@Composable
private fun InputMethodTile(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
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
private fun PaymentRow(payment: ReceivedPayment, matched: Boolean, onClick: (() -> Unit)?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        (if (matched) MoneyIn else MoneyOut).copy(alpha = 0.15f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = payment.provider.displayName().take(1),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (matched) MoneyIn else MoneyOut,
                )
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = payment.senderName ?: payment.provider.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${formatWhen(payment.occurredAt)} • ${payment.reference}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = formatMoney(payment.amount.minorUnits, payment.amount.currency),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
