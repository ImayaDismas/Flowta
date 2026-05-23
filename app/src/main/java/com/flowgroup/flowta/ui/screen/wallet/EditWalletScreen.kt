package com.flowgroup.flowta.ui.screen.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.ui.state.wallet.EditWalletEvent
import com.flowgroup.flowta.ui.state.wallet.EditWalletUiEvent
import com.flowgroup.flowta.ui.state.wallet.EditWalletUiState
import com.flowgroup.flowta.ui.viewmodel.wallet.EditWalletViewModel

@Composable
fun EditWalletScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: EditWalletViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                EditWalletUiEvent.Saved -> onSaved()
                EditWalletUiEvent.Deleted -> onDeleted()
            }
        }
    }

    EditWalletContent(uiState = uiState, onBack = onBack, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditWalletContent(
    uiState: EditWalletUiState,
    onBack: () -> Unit,
    onEvent: (EditWalletEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_wallet_title)) },
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
                EditWalletUiState.Loading -> Unit
                EditWalletUiState.NotFound -> Text(
                    text = stringResource(R.string.wallet_detail_not_found_message),
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                is EditWalletUiState.Content -> EditWalletForm(content = uiState, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun EditWalletForm(
    content: EditWalletUiState.Content,
    onEvent: (EditWalletEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.edit_wallet_type_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        WalletTypeChips(
            selected = content.type,
            onSelect = { onEvent(EditWalletEvent.TypeChanged(it)) },
        )

        OutlinedTextField(
            value = content.name,
            onValueChange = { onEvent(EditWalletEvent.NameChanged(it)) },
            label = { Text(stringResource(R.string.edit_wallet_name_label)) },
            singleLine = true,
            isError = content.nameError != null,
            supportingText = content.nameError?.let { error ->
                { Text(stringResource(error.messageRes), color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )

        OpeningBalanceReadOnly(
            minorUnits = content.openingBalanceMinor,
            currency = content.currency,
        )

        content.submitError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { onEvent(EditWalletEvent.Save) },
            enabled = !content.isSaving && !content.isDeleting,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            if (content.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = stringResource(R.string.edit_wallet_save),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        OutlinedButton(
            onClick = { onEvent(EditWalletEvent.DeleteRequested) },
            enabled = !content.isSaving && !content.isDeleting,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = stringResource(R.string.edit_wallet_delete))
        }
    }

    DeleteDialog(state = content, onEvent = onEvent)
}

@Composable
private fun OpeningBalanceReadOnly(minorUnits: Long, currency: CurrencyCode) {
    Column {
        Text(
            text = stringResource(R.string.edit_wallet_opening_balance_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "${currency.iso4217} ${formatThousands(minorUnits)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.edit_wallet_opening_balance_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeleteDialog(
    state: EditWalletUiState.Content,
    onEvent: (EditWalletEvent) -> Unit,
) {
    when (val dialog = state.deleteDialog) {
        null -> Unit
        EditWalletUiState.Content.DeleteDialog.Confirm -> AlertDialog(
            onDismissRequest = { onEvent(EditWalletEvent.DismissDialog) },
            title = { Text(stringResource(R.string.edit_wallet_delete_confirm_title)) },
            text = { Text(stringResource(R.string.edit_wallet_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { onEvent(EditWalletEvent.DeleteConfirmed) }) {
                    Text(
                        text = stringResource(R.string.edit_wallet_delete_confirm_cta),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(EditWalletEvent.DismissDialog) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
        is EditWalletUiState.Content.DeleteDialog.Blocked -> AlertDialog(
            onDismissRequest = { onEvent(EditWalletEvent.DismissDialog) },
            title = { Text(stringResource(R.string.edit_wallet_delete_blocked_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.edit_wallet_delete_blocked_message,
                        dialog.transactionCount,
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { onEvent(EditWalletEvent.DismissDialog) }) {
                    Text(stringResource(R.string.edit_wallet_delete_blocked_cta))
                }
            },
        )
    }
}

@Composable
private fun WalletTypeChips(
    selected: WalletType,
    onSelect: (WalletType) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(WalletType.entries) { type ->
            FilterChip(
                selected = type == selected,
                onClick = { onSelect(type) },
                label = { Text(stringResource(type.labelRes())) },
            )
        }
    }
}

private fun WalletType.labelRes(): Int = when (this) {
    WalletType.CASH -> R.string.wallet_type_cash
    WalletType.MPESA -> R.string.wallet_type_mpesa
    WalletType.AIRTEL_MONEY -> R.string.wallet_type_airtel
    WalletType.T_KASH -> R.string.wallet_type_tkash
    WalletType.BANK -> R.string.wallet_type_bank
    WalletType.OTHER -> R.string.wallet_type_other
}

private fun formatThousands(minorUnits: Long): String {
    val sign = if (minorUnits < 0L) "-" else ""
    val abs = kotlin.math.abs(minorUnits).toString()
    return sign + abs.reversed().chunked(3).joinToString(",").reversed()
}
