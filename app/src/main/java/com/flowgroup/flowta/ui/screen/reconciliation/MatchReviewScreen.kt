package com.flowgroup.flowta.ui.screen.reconciliation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.domain.model.ReceivedPayment
import com.flowgroup.flowta.domain.model.Transaction
import com.flowgroup.flowta.domain.model.WalletWithBalance
import com.flowgroup.flowta.ui.state.reconciliation.MatchReviewUiState
import com.flowgroup.flowta.ui.theme.MoneyIn
import com.flowgroup.flowta.ui.viewmodel.reconciliation.MatchReviewViewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun MatchReviewScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: MatchReviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is MatchReviewUiState.Done) onDone()
    }

    MatchReviewContent(
        uiState = uiState,
        onBack = onBack,
        onConfirm = viewModel::onConfirmMatch,
        onNotAMatch = viewModel::onNotAMatch,
        onSelectWallet = viewModel::onSelectWallet,
        onRecordSale = viewModel::onRecordSale,
        onDismiss = viewModel::onDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchReviewContent(
    uiState: MatchReviewUiState,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onNotAMatch: () -> Unit,
    onSelectWallet: (String) -> Unit,
    onRecordSale: () -> Unit,
    onDismiss: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.match_review_title)) },
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
        when (uiState) {
            MatchReviewUiState.Loading, MatchReviewUiState.Done -> Unit
            is MatchReviewUiState.Error -> Text(
                text = uiState.message,
                modifier = Modifier.padding(padding).padding(16.dp),
                color = MaterialTheme.colorScheme.error,
            )
            is MatchReviewUiState.Content -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PaymentCard(uiState.payment)

                if (uiState.showSuggestion && uiState.suggestion != null) {
                    SuggestionCard(uiState.suggestion)
                    Button(
                        onClick = onConfirm,
                        enabled = !uiState.working,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.match_review_confirm)) }
                    TextButton(
                        onClick = onNotAMatch,
                        enabled = !uiState.working,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.match_review_not_a_match)) }
                } else {
                    RecordAsSaleSection(
                        wallets = uiState.wallets,
                        selectedWalletId = uiState.selectedWalletId,
                        working = uiState.working,
                        onSelectWallet = onSelectWallet,
                        onRecordSale = onRecordSale,
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    enabled = !uiState.working,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.match_review_dismiss)) }
            }
        }
    }
}

@Composable
private fun PaymentCard(payment: ReceivedPayment) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = payment.provider.displayName(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatMoney(payment.amount.minorUnits, payment.amount.currency),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${payment.senderName ?: ""} • ${payment.reference}".trim().removePrefix("• "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatWhen(payment.occurredAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: Transaction) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.match_review_suggested),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider()
            Column {
                Text(
                    text = suggestion.note?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.match_review_suggested),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${formatWhen(suggestion.occurredAt)} • " +
                        formatMoney(suggestion.amount.minorUnits, suggestion.amount.currency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.match_review_exact),
                style = MaterialTheme.typography.labelMedium,
                color = MoneyIn,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecordAsSaleSection(
    wallets: List<WalletWithBalance>,
    selectedWalletId: String?,
    working: Boolean,
    onSelectWallet: (String) -> Unit,
    onRecordSale: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.match_review_no_suggestion),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (wallets.isEmpty()) {
            Text(
                text = stringResource(R.string.match_review_no_wallet),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            Text(
                text = stringResource(R.string.match_review_choose_wallet),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                wallets.forEach { wallet ->
                    FilterChip(
                        selected = wallet.wallet.id == selectedWalletId,
                        onClick = { onSelectWallet(wallet.wallet.id) },
                        label = { Text(wallet.wallet.name) },
                    )
                }
            }
            Button(
                onClick = onRecordSale,
                enabled = !working && selectedWalletId != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.match_review_record_sale)) }
        }
    }
}
