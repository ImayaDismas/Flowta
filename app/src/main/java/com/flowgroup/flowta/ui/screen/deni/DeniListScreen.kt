package com.flowgroup.flowta.ui.screen.deni

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.ClientDeni
import com.flowgroup.flowta.ui.components.EmptyState
import com.flowgroup.flowta.ui.state.deni.DeniListUiState
import com.flowgroup.flowta.ui.viewmodel.deni.DeniListViewModel

@Composable
fun DeniListScreen(
    onBack: () -> Unit,
    onAddClient: () -> Unit,
    onOpenClient: (String) -> Unit,
    viewModel: DeniListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DeniListContent(
        uiState = uiState,
        onBack = onBack,
        onAddClient = onAddClient,
        onOpenClient = onOpenClient,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeniListContent(
    uiState: DeniListUiState,
    onBack: () -> Unit,
    onAddClient: () -> Unit,
    onOpenClient: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.deni_title)) },
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClient,
                icon = { Icon(Icons.Outlined.PersonAdd, contentDescription = null) },
                text = { Text(stringResource(R.string.deni_add_customer)) },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                DeniListUiState.Loading -> Unit
                DeniListUiState.Empty -> EmptyState(
                    icon = Icons.Outlined.Group,
                    title = stringResource(R.string.deni_empty_title),
                    message = stringResource(R.string.deni_empty_message),
                )
                is DeniListUiState.Error -> EmptyState(
                    icon = Icons.Outlined.Group,
                    title = stringResource(R.string.deni_empty_title),
                    message = uiState.message.ifBlank { stringResource(R.string.deni_empty_message) },
                )
                is DeniListUiState.Content -> DeniList(
                    content = uiState,
                    onOpenClient = onOpenClient,
                )
            }
        }
    }
}

@Composable
private fun DeniList(
    content: DeniListUiState.Content,
    onOpenClient: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "total") {
            TotalOwedCard(
                totalMinor = content.totalOutstandingMinor,
                currency = content.currency,
            )
        }
        items(content.clients, key = { it.client.id }) { item ->
            ClientRow(item = item, onClick = { onOpenClient(item.client.id) })
        }
    }
}

@Composable
private fun TotalOwedCard(totalMinor: Long, currency: CurrencyCode) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.deni_total_owed_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = formatMoney(totalMinor, currency),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun ClientRow(item: ClientDeni, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.client.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                item.client.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = formatMoney(item.outstandingMinor, item.client.currency),
                style = MaterialTheme.typography.titleMedium,
                color = if (item.outstandingMinor > 0L) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal fun formatMoney(minorUnits: Long, currency: CurrencyCode): String {
    val negative = minorUnits < 0L
    val abs = if (negative) -minorUnits else minorUnits
    val grouped = abs.toString().reversed().chunked(3).joinToString(",").reversed()
    return if (negative) "-${currency.iso4217} $grouped" else "${currency.iso4217} $grouped"
}
