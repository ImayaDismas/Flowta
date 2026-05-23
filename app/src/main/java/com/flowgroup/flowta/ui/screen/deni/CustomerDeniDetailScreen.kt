package com.flowgroup.flowta.ui.screen.deni

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.flowgroup.flowta.domain.model.CustomerDeniDetail
import com.flowgroup.flowta.domain.model.DeniEntry
import com.flowgroup.flowta.domain.model.DeniEntryType
import com.flowgroup.flowta.ui.components.EmptyState
import com.flowgroup.flowta.ui.state.deni.CustomerDeniDetailEvent
import com.flowgroup.flowta.ui.state.deni.CustomerDeniDetailUiState
import com.flowgroup.flowta.ui.theme.MoneyIn
import com.flowgroup.flowta.ui.theme.MoneyOut
import com.flowgroup.flowta.ui.viewmodel.deni.CustomerDeniDetailViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun CustomerDeniDetailScreen(
    onBack: () -> Unit,
    viewModel: CustomerDeniDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CustomerDeniDetailContent(
        uiState = uiState,
        onBack = onBack,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDeniDetailContent(
    uiState: CustomerDeniDetailUiState,
    onBack: () -> Unit,
    onEvent: (CustomerDeniDetailEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (uiState as? CustomerDeniDetailUiState.Content)?.detail?.customer?.name
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
                CustomerDeniDetailUiState.Loading -> Unit
                CustomerDeniDetailUiState.NotFound -> EmptyState(
                    icon = Icons.Outlined.Group,
                    title = stringResource(R.string.customer_detail_not_found_title),
                    message = stringResource(R.string.customer_detail_not_found_message),
                )
                is CustomerDeniDetailUiState.Error -> EmptyState(
                    icon = Icons.Outlined.Group,
                    title = stringResource(R.string.customer_detail_not_found_title),
                    message = uiState.message.ifBlank {
                        stringResource(R.string.customer_detail_not_found_message)
                    },
                )
                is CustomerDeniDetailUiState.Content -> {
                    CustomerDeniBody(content = uiState, onEvent = onEvent)
                    if (uiState.dialog != null) {
                        AmountDialog(content = uiState, onEvent = onEvent)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerDeniBody(
    content: CustomerDeniDetailUiState.Content,
    onEvent: (CustomerDeniDetailEvent) -> Unit,
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
                    onClick = { onEvent(CustomerDeniDetailEvent.RecordPaymentClicked) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = stringResource(R.string.customer_detail_record_payment),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Button(
                    onClick = { onEvent(CustomerDeniDetailEvent.AddCreditClicked) },
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
private fun OutstandingCard(detail: CustomerDeniDetail) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            detail.customer.phone?.takeIf { it.isNotBlank() }?.let { phone ->
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
                text = formatMoney(detail.outstandingMinor, detail.customer.currency),
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
    content: CustomerDeniDetailUiState.Content,
    onEvent: (CustomerDeniDetailEvent) -> Unit,
) {
    val isCredit = content.dialog == CustomerDeniDetailUiState.Content.Dialog.CREDIT
    val currency = content.detail.customer.currency
    AlertDialog(
        onDismissRequest = { onEvent(CustomerDeniDetailEvent.DialogDismissed) },
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
                    onValueChange = { onEvent(CustomerDeniDetailEvent.AmountChanged(it)) },
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
                    onValueChange = { onEvent(CustomerDeniDetailEvent.NoteChanged(it)) },
                    label = { Text(stringResource(R.string.deni_dialog_note_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
                content.submitError?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider()
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onEvent(CustomerDeniDetailEvent.DialogConfirmed) },
                enabled = !content.isSubmitting,
            ) { Text(stringResource(R.string.deni_dialog_save)) }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(CustomerDeniDetailEvent.DialogDismissed) }) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

private fun formatDate(instant: kotlinx.datetime.Instant): String {
    val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = ldt.dayOfMonth.toString().padStart(2, '0')
    val month = ldt.monthNumber.toString().padStart(2, '0')
    return "$day/$month/${ldt.year}"
}
