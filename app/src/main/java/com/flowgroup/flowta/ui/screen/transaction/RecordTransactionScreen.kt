package com.flowgroup.flowta.ui.screen.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.TransactionType
import com.flowgroup.flowta.domain.model.Wallet
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.ui.state.transaction.RecordTransactionEvent
import com.flowgroup.flowta.ui.state.transaction.RecordTransactionUiEvent
import com.flowgroup.flowta.ui.state.transaction.RecordTransactionUiState
import com.flowgroup.flowta.ui.screen.reconciliation.formatMoney
import com.flowgroup.flowta.ui.theme.FlowtaTheme
import com.flowgroup.flowta.ui.viewmodel.transaction.RecordTransactionViewModel
import kotlinx.datetime.Instant

@Composable
fun RecordTransactionScreen(
    onClose: () -> Unit,
    onRecorded: () -> Unit,
    onAddWallet: () -> Unit,
    viewModel: RecordTransactionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RecordTransactionUiEvent.Recorded -> onRecorded()
            }
        }
    }

    RecordTransactionContent(
        uiState = uiState,
        onClose = onClose,
        onAddWallet = onAddWallet,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordTransactionContent(
    uiState: RecordTransactionUiState,
    onClose: () -> Unit,
    onAddWallet: () -> Unit,
    onEvent: (RecordTransactionEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.record_tx_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
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
                RecordTransactionUiState.Loading -> Unit
                RecordTransactionUiState.NoWallets -> NoWalletsState(onAddWallet = onAddWallet)
                is RecordTransactionUiState.Content -> RecordTransactionForm(
                    content = uiState,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun NoWalletsState(onAddWallet: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.record_tx_no_wallets_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.record_tx_no_wallets_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(
            onClick = onAddWallet,
            modifier = Modifier.padding(top = 24.dp),
        ) { Text(stringResource(R.string.add_wallet_cta)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordTransactionForm(
    content: RecordTransactionUiState.Content,
    onEvent: (RecordTransactionEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        TypeSegmentedRow(
            selected = content.type,
            onSelect = { onEvent(RecordTransactionEvent.TypeChanged(it)) },
        )

        WalletDropdown(
            content = content,
            onSelect = { onEvent(RecordTransactionEvent.WalletChanged(it)) },
        )

        OutlinedTextField(
            value = content.amountInput,
            onValueChange = { onEvent(RecordTransactionEvent.AmountChanged(it)) },
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

        if (content.type == TransactionType.SALE) {
            CreditSection(content = content, onEvent = onEvent)
        }

        OutlinedTextField(
            value = content.note,
            onValueChange = { onEvent(RecordTransactionEvent.NoteChanged(it)) },
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
            onClick = { onEvent(RecordTransactionEvent.Submit) },
            enabled = !content.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            if (content.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = stringResource(
                        if (content.type == TransactionType.SALE) R.string.record_tx_cta_sale
                        else R.string.record_tx_cta_expense
                    ),
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
    content: RecordTransactionUiState.Content,
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

@Composable
private fun CreditSection(
    content: RecordTransactionUiState.Content,
    onEvent: (RecordTransactionEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.record_tx_on_credit_label),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = content.onCredit,
                onCheckedChange = { onEvent(RecordTransactionEvent.CreditToggled(it)) },
            )
        }

        if (content.onCredit) {
            ClientDropdown(content = content, onEvent = onEvent)

            if (content.addingNewClient) {
                OutlinedTextField(
                    value = content.newClientName,
                    onValueChange = { onEvent(RecordTransactionEvent.NewClientNameChanged(it)) },
                    label = { Text(stringResource(R.string.record_tx_new_client_name_label)) },
                    placeholder = { Text(stringResource(R.string.add_customer_name_hint)) },
                    singleLine = true,
                    isError = content.clientError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = content.newClientPhone,
                    onValueChange = { onEvent(RecordTransactionEvent.NewClientPhoneChanged(it)) },
                    label = { Text(stringResource(R.string.record_tx_new_client_phone_label)) },
                    placeholder = { Text(stringResource(R.string.add_customer_phone_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (content.clientError) {
                Text(
                    text = stringResource(R.string.record_tx_error_client_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            val currency = content.selectedWallet.openingBalance.currency
            val total = content.amountInput.toLongOrNull() ?: 0L
            val credit = content.creditAmountInput.toLongOrNull() ?: 0L
            val paidNow = (total - credit).coerceAtLeast(0L)
            OutlinedTextField(
                value = content.creditAmountInput,
                onValueChange = { onEvent(RecordTransactionEvent.CreditAmountChanged(it)) },
                label = { Text(stringResource(R.string.record_tx_credit_amount_label, currency.iso4217)) },
                placeholder = { Text("0") },
                singleLine = true,
                isError = content.creditError != null,
                supportingText = {
                    if (content.creditError != null) {
                        Text(
                            text = stringResource(content.creditError.messageRes),
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        Text(stringResource(R.string.record_tx_paid_now, formatMoney(paidNow, currency)))
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientDropdown(
    content: RecordTransactionUiState.Content,
    onEvent: (RecordTransactionEvent) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val fieldValue = when {
        content.addingNewClient -> stringResource(R.string.record_tx_client_new)
        else -> content.selectedClientName.orEmpty()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = fieldValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.record_tx_client_label)) },
            placeholder = { Text(stringResource(R.string.record_tx_client_select_hint)) },
            isError = content.clientError,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            content.clients.forEach { clientDeni ->
                DropdownMenuItem(
                    text = { Text(clientDeni.client.name) },
                    onClick = {
                        onEvent(RecordTransactionEvent.ClientSelected(clientDeni.client.id))
                        expanded = false
                    },
                )
            }
            if (content.clients.isNotEmpty()) {
                HorizontalDivider()
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.record_tx_client_new)) },
                onClick = {
                    onEvent(RecordTransactionEvent.NewClientSelected)
                    expanded = false
                },
            )
        }
    }
}

private fun TransactionType.labelRes(): Int = when (this) {
    TransactionType.SALE -> R.string.record_tx_type_sale
    TransactionType.EXPENSE -> R.string.record_tx_type_expense
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RecordTransactionPreview() {
    val sample = Wallet(
        id = "w1",
        businessId = "b1",
        name = "Cash drawer",
        type = WalletType.CASH,
        openingBalance = Money(0L, CurrencyCode.KES),
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
    )
    FlowtaTheme {
        RecordTransactionContent(
            uiState = RecordTransactionUiState.Content(
                wallets = listOf(sample),
                selectedWalletId = sample.id,
            ),
            onClose = {},
            onAddWallet = {},
            onEvent = {},
        )
    }
}
