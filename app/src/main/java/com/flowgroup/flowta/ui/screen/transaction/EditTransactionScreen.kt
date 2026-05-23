package com.flowgroup.flowta.ui.screen.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.ui.components.EmptyState
import com.flowgroup.flowta.ui.state.transaction.EditTransactionEvent
import com.flowgroup.flowta.ui.state.transaction.EditTransactionUiEvent
import com.flowgroup.flowta.ui.state.transaction.EditTransactionUiState
import com.flowgroup.flowta.ui.viewmodel.transaction.EditTransactionViewModel

@Composable
fun EditTransactionScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditTransactionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                EditTransactionUiEvent.Saved -> onSaved()
            }
        }
    }

    EditTransactionContent(
        uiState = uiState,
        onBack = onBack,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTransactionContent(
    uiState: EditTransactionUiState,
    onBack: () -> Unit,
    onEvent: (EditTransactionEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_tx_title)) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState) {
                EditTransactionUiState.Loading -> Unit
                EditTransactionUiState.NotFound -> EmptyState(
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    title = stringResource(R.string.transaction_detail_not_found_title),
                    message = stringResource(R.string.transaction_detail_not_found_message),
                )
                is EditTransactionUiState.Content -> EditTransactionForm(
                    content = uiState,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTransactionForm(
    content: EditTransactionUiState.Content,
    onEvent: (EditTransactionEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        TypeSegmentedRow(
            selected = content.type,
            onSelect = { onEvent(EditTransactionEvent.TypeChanged(it)) },
        )

        WalletDropdown(
            content = content,
            onSelect = { onEvent(EditTransactionEvent.WalletChanged(it)) },
        )

        OutlinedTextField(
            value = content.amountInput,
            onValueChange = { onEvent(EditTransactionEvent.AmountChanged(it)) },
            label = {
                Text(
                    stringResource(
                        R.string.record_tx_amount_label,
                        content.selectedWallet.openingBalance.currency.iso4217,
                    )
                )
            },
            placeholder = { Text("0") },
            singleLine = true,
            isError = content.amountError != null,
            supportingText = content.amountError?.let { error ->
                { Text(stringResource(error.messageRes), color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = content.note,
            onValueChange = { onEvent(EditTransactionEvent.NoteChanged(it)) },
            label = { Text(stringResource(R.string.record_tx_note_label)) },
            placeholder = { Text(stringResource(R.string.record_tx_note_hint)) },
            singleLine = false,
            minLines = 2,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
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
            onClick = { onEvent(EditTransactionEvent.Save) },
            enabled = !content.isSaving,
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
                    text = stringResource(R.string.edit_tx_cta_save),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSegmentedRow(
    selected: TransactionType,
    onSelect: (TransactionType) -> Unit,
) {
    val options = TransactionType.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, type ->
            SegmentedButton(
                selected = selected == type,
                onClick = { onSelect(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = { Text(stringResource(type.labelRes())) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletDropdown(
    content: EditTransactionUiState.Content,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = content.selectedWallet.name,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.record_tx_wallet_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            content.wallets.forEach { wallet ->
                DropdownMenuItem(
                    text = { Text(wallet.name) },
                    onClick = {
                        onSelect(wallet.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun TransactionType.labelRes(): Int = when (this) {
    TransactionType.SALE -> R.string.record_tx_type_sale
    TransactionType.EXPENSE -> R.string.record_tx_type_expense
}
