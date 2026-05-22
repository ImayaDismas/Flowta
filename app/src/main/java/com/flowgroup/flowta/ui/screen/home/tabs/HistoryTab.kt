package com.flowgroup.flowta.ui.screen.home.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.ui.components.EmptyState
import com.flowgroup.flowta.ui.components.TransactionListItem
import com.flowgroup.flowta.ui.state.home.HistoryTabUiState
import com.flowgroup.flowta.ui.viewmodel.home.HistoryTabViewModel

@Composable
fun HistoryTab(
    modifier: Modifier = Modifier,
    viewModel: HistoryTabViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryTabContent(uiState = uiState, modifier = modifier)
}

@Composable
private fun HistoryTabContent(
    uiState: HistoryTabUiState,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        HistoryTabUiState.Loading -> Unit
        is HistoryTabUiState.Error -> EmptyState(
            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
            title = stringResource(R.string.history_empty_title),
            message = uiState.message.ifBlank { stringResource(R.string.history_empty_message) },
            modifier = modifier,
        )
        is HistoryTabUiState.Content -> if (uiState.items.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                title = stringResource(R.string.history_empty_title),
                message = stringResource(R.string.history_empty_message),
                modifier = modifier,
            )
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.items, key = { it.transaction.id }) { item ->
                    TransactionListItem(item = item)
                }
            }
        }
    }
}
