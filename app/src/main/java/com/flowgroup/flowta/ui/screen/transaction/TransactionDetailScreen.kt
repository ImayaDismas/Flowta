package com.flowgroup.flowta.ui.screen.transaction

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.TransactionWithWallet
import com.flowgroup.flowta.ui.components.EmptyState
import com.flowgroup.flowta.ui.state.transaction.TransactionDetailEvent
import com.flowgroup.flowta.ui.state.transaction.TransactionDetailUiEvent
import com.flowgroup.flowta.ui.state.transaction.TransactionDetailUiState
import com.flowgroup.flowta.ui.theme.MoneyIn
import com.flowgroup.flowta.ui.theme.MoneyOut
import com.flowgroup.flowta.ui.viewmodel.transaction.TransactionDetailViewModel
import androidx.compose.runtime.LaunchedEffect
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TransactionDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                TransactionDetailUiEvent.Deleted -> onBack()
            }
        }
    }

    TransactionDetailContent(
        uiState = uiState,
        onBack = onBack,
        onEdit = { onEdit(viewModel.transactionId) },
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDetailContent(
    uiState: TransactionDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onEvent: (TransactionDetailEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.transaction_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
                actions = {
                    if (uiState is TransactionDetailUiState.Content) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.transaction_detail_edit),
                            )
                        }
                        IconButton(onClick = { onEvent(TransactionDetailEvent.DeleteRequested) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.transaction_detail_delete),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                TransactionDetailUiState.Loading -> Unit
                TransactionDetailUiState.NotFound -> EmptyState(
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    title = stringResource(R.string.transaction_detail_not_found_title),
                    message = stringResource(R.string.transaction_detail_not_found_message),
                )
                is TransactionDetailUiState.Error -> EmptyState(
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    title = stringResource(R.string.transaction_detail_not_found_title),
                    message = uiState.message.ifBlank {
                        stringResource(R.string.transaction_detail_not_found_message)
                    },
                )
                is TransactionDetailUiState.Content -> {
                    ReceiptBody(detail = uiState.detail)
                    if (uiState.confirmingDelete) {
                        DeleteDialog(
                            onConfirm = { onEvent(TransactionDetailEvent.DeleteConfirmed) },
                            onDismiss = { onEvent(TransactionDetailEvent.DeleteDismissed) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptBody(detail: TransactionWithWallet) {
    val context = LocalContext.current
    val transaction = detail.transaction
    val isSale = transaction.type == TransactionType.SALE
    val accent: Color = if (isSale) MoneyIn else MoneyOut

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
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
                Text(
                    text = transaction.note?.takeIf { it.isNotBlank() }
                        ?: stringResource(transaction.type.labelRes()),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.transaction_detail_amount_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatSignedMoney(
                        transaction.amount.minorUnits,
                        transaction.amount.currency,
                        isSale,
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    color = accent,
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                DetailRow(
                    label = stringResource(R.string.transaction_detail_datetime_label),
                    value = formatDateTime(transaction.occurredAt),
                )
                DetailRow(
                    label = stringResource(R.string.transaction_detail_wallet_label),
                    value = detail.walletName,
                )
                DetailRow(
                    label = stringResource(R.string.transaction_detail_type_label),
                    value = stringResource(transaction.type.labelRes()),
                )
                DetailRow(
                    label = stringResource(R.string.transaction_detail_reference_label),
                    value = transaction.id,
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.transaction_detail_receipt_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        OutlinedButton(
            onClick = {
                Toast.makeText(
                    context,
                    context.getString(R.string.transaction_detail_share_coming_soon),
                    Toast.LENGTH_SHORT,
                ).show()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(stringResource(R.string.transaction_detail_share))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.transaction_detail_delete_title)) },
        text = { Text(stringResource(R.string.transaction_detail_delete_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.transaction_detail_delete_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

private fun TransactionType.labelRes(): Int = when (this) {
    TransactionType.SALE -> R.string.record_tx_type_sale
    TransactionType.EXPENSE -> R.string.record_tx_type_expense
}

private fun formatSignedMoney(minorUnits: Long, currency: CurrencyCode, isSale: Boolean): String {
    val sign = if (isSale) "+" else "-"
    val grouped = minorUnits.toString().reversed().chunked(3).joinToString(",").reversed()
    return "$sign${currency.iso4217} $grouped"
}

private fun formatDateTime(instant: Instant): String {
    val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = ldt.dayOfMonth.toString().padStart(2, '0')
    val month = ldt.monthNumber.toString().padStart(2, '0')
    val hour = ldt.hour.toString().padStart(2, '0')
    val minute = ldt.minute.toString().padStart(2, '0')
    return "$day/$month/${ldt.year} · $hour:$minute"
}
