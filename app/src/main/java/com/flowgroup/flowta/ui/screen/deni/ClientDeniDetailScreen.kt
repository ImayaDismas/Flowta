package com.flowgroup.flowta.ui.screen.deni

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.domain.model.ClientDeniDetail
import com.flowgroup.flowta.domain.model.DeniEntry
import com.flowgroup.flowta.domain.model.DeniEntryType
import com.flowgroup.flowta.domain.model.WalletWithBalance
import com.flowgroup.flowta.ui.components.EmptyState
import com.flowgroup.flowta.ui.state.deni.ClientDeniDetailEvent
import com.flowgroup.flowta.ui.state.deni.ClientDeniDetailUiState
import com.flowgroup.flowta.ui.theme.MoneyIn
import com.flowgroup.flowta.ui.theme.MoneyOut
import com.flowgroup.flowta.ui.viewmodel.deni.ClientDeniDetailViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ClientDeniDetailScreen(
    onBack: () -> Unit,
    viewModel: ClientDeniDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ClientDeniDetailContent(
        uiState = uiState,
        onBack = onBack,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientDeniDetailContent(
    uiState: ClientDeniDetailUiState,
    onBack: () -> Unit,
    onEvent: (ClientDeniDetailEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (uiState as? ClientDeniDetailUiState.Content)?.detail?.client?.name
                            ?: stringResource(R.string.customer_detail_title),
                    )
                },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                ClientDeniDetailUiState.Loading -> Unit
                ClientDeniDetailUiState.NotFound -> EmptyState(
                    icon = Icons.Outlined.Group,
                    title = stringResource(R.string.customer_detail_not_found_title),
                    message = stringResource(R.string.customer_detail_not_found_message),
                )
                is ClientDeniDetailUiState.Error -> EmptyState(
                    icon = Icons.Outlined.Group,
                    title = stringResource(R.string.customer_detail_not_found_title),
                    message = uiState.message.ifBlank {
                        stringResource(R.string.customer_detail_not_found_message)
                    },
                )
                is ClientDeniDetailUiState.Content -> {
                    ClientDeniBody(content = uiState, onEvent = onEvent)
                    if (uiState.dialog != null) {
                        AmountDialog(content = uiState, onEvent = onEvent)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientDeniBody(
    content: ClientDeniDetailUiState.Content,
    onEvent: (ClientDeniDetailEvent) -> Unit,
) {
    val detail = content.detail
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "header") { OutstandingCard(detail) }
        item(key = "actions") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onEvent(ClientDeniDetailEvent.RecordPaymentClicked) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = stringResource(R.string.customer_detail_record_payment),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Button(
                    onClick = { onEvent(ClientDeniDetailEvent.AddCreditClicked) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = stringResource(R.string.customer_detail_add_credit),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
        item(key = "history_label") {
            Text(
                text = stringResource(R.string.customer_detail_history),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (detail.entries.isEmpty()) {
            item(key = "history_empty") {
                Text(
                    text = stringResource(R.string.customer_detail_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(detail.entries, key = { it.id }) { entry ->
                DeniEntryRow(entry)
            }
        }
    }
}

@Composable
private fun OutstandingCard(detail: ClientDeniDetail) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            detail.client.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                Text(
                    text = phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.customer_detail_outstanding_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = formatMoney(detail.outstandingMinor, detail.client.currency),
                style = MaterialTheme.typography.headlineMedium,
                color = if (detail.outstandingMinor > 0L) MoneyOut else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun DeniEntryRow(entry: DeniEntry) {
    val isCredit = entry.type == DeniEntryType.CREDIT
    val accent = if (isCredit) MoneyOut else MoneyIn
    val sign = if (isCredit) "+" else "−"
    Surface(
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
                    text = stringResource(
                        if (isCredit) R.string.deni_entry_credit else R.string.deni_entry_payment
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = entry.note?.takeIf { it.isNotBlank() }?.let { "$it · ${formatDate(entry.occurredAt)}" }
                        ?: formatDate(entry.occurredAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "$sign${formatMoney(entry.amount.minorUnits, entry.amount.currency)}",
                style = MaterialTheme.typography.titleMedium,
                color = accent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmountDialog(
    content: ClientDeniDetailUiState.Content,
    onEvent: (ClientDeniDetailEvent) -> Unit,
) {
    val isCredit = content.dialog == ClientDeniDetailUiState.Content.Dialog.CREDIT
    val currency = content.detail.client.currency
    AlertDialog(
        onDismissRequest = { onEvent(ClientDeniDetailEvent.DialogDismissed) },
        title = {
            Text(
                stringResource(
                    if (isCredit) R.string.deni_credit_dialog_title else R.string.deni_payment_dialog_title
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = content.amountInput,
                    onValueChange = { onEvent(ClientDeniDetailEvent.AmountChanged(it)) },
                    label = { Text(stringResource(R.string.deni_dialog_amount_label, currency.iso4217)) },
                    placeholder = { Text("0") },
                    singleLine = true,
                    isError = content.amountError,
                    supportingText = if (content.amountError) {
                        { Text(stringResource(R.string.record_tx_error_amount_invalid), color = MaterialTheme.colorScheme.error) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = content.noteInput,
                    onValueChange = { onEvent(ClientDeniDetailEvent.NoteChanged(it)) },
                    label = { Text(stringResource(R.string.deni_dialog_note_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (content.wallets.isNotEmpty()) {
                    WalletPicker(
                        label = stringResource(
                            if (isCredit) R.string.deni_wallet_picker_credit_label
                            else R.string.deni_wallet_picker_payment_label
                        ),
                        wallets = content.wallets,
                        selectedWalletId = content.selectedWalletId,
                        onSelect = { onEvent(ClientDeniDetailEvent.WalletSelected(it)) },
                    )
                }
                content.submitError?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider()
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onEvent(ClientDeniDetailEvent.DialogConfirmed) },
                enabled = !content.isSubmitting,
            ) { Text(stringResource(R.string.deni_dialog_save)) }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(ClientDeniDetailEvent.DialogDismissed) }) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WalletPicker(
    label: String,
    wallets: List<WalletWithBalance>,
    selectedWalletId: String?,
    onSelect: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedWalletId == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.deni_wallet_picker_none)) },
            )
            wallets.forEach { wb ->
                FilterChip(
                    selected = selectedWalletId == wb.wallet.id,
                    onClick = { onSelect(wb.wallet.id) },
                    label = {
                        Text("${wb.wallet.name} · ${formatMoney(wb.currentBalanceMinor, wb.wallet.openingBalance.currency)}")
                    },
                )
            }
        }
    }
}

private fun formatDate(instant: kotlinx.datetime.Instant): String {
    val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = ldt.dayOfMonth.toString().padStart(2, '0')
    val month = ldt.monthNumber.toString().padStart(2, '0')
    return "$day/$month/${ldt.year}"
}
