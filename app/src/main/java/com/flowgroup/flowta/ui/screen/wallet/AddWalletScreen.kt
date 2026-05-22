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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.ui.state.wallet.AddWalletEvent
import com.flowgroup.flowta.ui.state.wallet.AddWalletUiEvent
import com.flowgroup.flowta.ui.state.wallet.AddWalletUiState
import com.flowgroup.flowta.ui.theme.FlowtaTheme
import com.flowgroup.flowta.ui.viewmodel.wallet.AddWalletViewModel

@Composable
fun AddWalletScreen(
    onClose: () -> Unit,
    onCreated: () -> Unit,
    viewModel: AddWalletViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AddWalletUiEvent.Created -> onCreated()
            }
        }
    }

    AddWalletContent(
        uiState = uiState,
        onClose = onClose,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWalletContent(
    uiState: AddWalletUiState,
    onClose: () -> Unit,
    onEvent: (AddWalletEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_wallet_title)) },
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
                AddWalletUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(32.dp)
                                .padding(top = 32.dp),
                        )
                    }
                }
                AddWalletUiState.MissingBusiness -> {
                    Text(
                        text = stringResource(R.string.add_wallet_missing_business),
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                is AddWalletUiState.Content -> AddWalletForm(content = uiState, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun AddWalletForm(
    content: AddWalletUiState.Content,
    onEvent: (AddWalletEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.add_wallet_type_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        WalletTypeChips(
            selected = content.type,
            onSelect = { onEvent(AddWalletEvent.TypeChanged(it)) },
        )

        OutlinedTextField(
            value = content.name,
            onValueChange = { onEvent(AddWalletEvent.NameChanged(it)) },
            label = { Text(stringResource(R.string.add_wallet_name_label)) },
            placeholder = { Text(stringResource(content.type.hintRes())) },
            singleLine = true,
            isError = content.nameError != null,
            supportingText = content.nameError?.let { error ->
                { Text(stringResource(error.messageRes), color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = content.openingBalanceInput,
            onValueChange = { onEvent(AddWalletEvent.OpeningBalanceChanged(it)) },
            label = {
                Text(
                    stringResource(
                        R.string.add_wallet_opening_balance_label,
                        content.currency.iso4217,
                    )
                )
            },
            placeholder = { Text("0") },
            singleLine = true,
            isError = content.balanceError != null,
            supportingText = content.balanceError?.let { error ->
                { Text(stringResource(error.messageRes), color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
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
            onClick = { onEvent(AddWalletEvent.Submit) },
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
                    text = stringResource(R.string.add_wallet_cta),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
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

private fun WalletType.hintRes(): Int = when (this) {
    WalletType.CASH -> R.string.add_wallet_hint_cash
    WalletType.MPESA -> R.string.add_wallet_hint_mpesa
    WalletType.AIRTEL_MONEY -> R.string.add_wallet_hint_airtel
    WalletType.T_KASH -> R.string.add_wallet_hint_tkash
    WalletType.BANK -> R.string.add_wallet_hint_bank
    WalletType.OTHER -> R.string.add_wallet_hint_other
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AddWalletPreview() {
    FlowtaTheme {
        AddWalletContent(
            uiState = AddWalletUiState.Content(currency = CurrencyCode.KES),
            onClose = {},
            onEvent = {},
        )
    }
}
