package com.flowgroup.flowta.ui.screen.home.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.ui.components.EmptyState
import com.flowgroup.flowta.ui.components.WalletListItem
import com.flowgroup.flowta.ui.state.home.WalletsTabUiState
import com.flowgroup.flowta.ui.viewmodel.home.WalletsTabViewModel

@Composable
fun WalletsTab(
    modifier: Modifier = Modifier,
    viewModel: WalletsTabViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WalletsTabContent(uiState = uiState, modifier = modifier)
}

@Composable
private fun WalletsTabContent(
    uiState: WalletsTabUiState,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        WalletsTabUiState.Loading -> Unit
        is WalletsTabUiState.Error -> EmptyState(
            icon = Icons.Outlined.AccountBalanceWallet,
            title = stringResource(R.string.wallets_empty_title),
            message = uiState.message.ifBlank { stringResource(R.string.wallets_empty_message) },
            modifier = modifier,
        )
        is WalletsTabUiState.Content -> if (uiState.items.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.AccountBalanceWallet,
                title = stringResource(R.string.wallets_empty_title),
                message = stringResource(R.string.wallets_empty_message),
                modifier = modifier,
            )
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.wallets_section_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(uiState.items, key = { it.wallet.id }) { item ->
                    WalletListItem(item = item)
                }
            }
        }
    }
}
