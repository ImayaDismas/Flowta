package com.flowgroup.flowta.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.ui.screen.home.tabs.DashboardTab
import com.flowgroup.flowta.ui.screen.home.tabs.HistoryTab
import com.flowgroup.flowta.ui.screen.home.tabs.InsightsTab
import com.flowgroup.flowta.ui.screen.home.tabs.WalletsTab
import com.flowgroup.flowta.ui.state.home.HomeTab
import com.flowgroup.flowta.ui.state.home.HomeUiState
import com.flowgroup.flowta.ui.theme.FlowtaTheme
import com.flowgroup.flowta.ui.viewmodel.home.HomeViewModel

@Composable
fun HomeScreen(
    onAddWallet: () -> Unit,
    onRecordSale: () -> Unit,
    onRecordExpense: () -> Unit,
    onRecordTransaction: () -> Unit,
    onOpenWallet: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onAddWallet = onAddWallet,
        onRecordSale = onRecordSale,
        onRecordExpense = onRecordExpense,
        onRecordTransaction = onRecordTransaction,
        onOpenWallet = onOpenWallet,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onAddWallet: () -> Unit,
    onRecordSale: () -> Unit,
    onRecordExpense: () -> Unit,
    onRecordTransaction: () -> Unit,
    onOpenWallet: (String) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.Dashboard) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text(
                            text = (uiState as? HomeUiState.Content)?.businessName
                                ?: stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        (uiState as? HomeUiState.Content)?.let {
                            Text(
                                text = it.currency.iso4217,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            HomeBottomBar(
                selected = selectedTab,
                onSelect = { selectedTab = it },
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                HomeTab.Dashboard -> Unit
                HomeTab.Wallets -> FloatingActionButton(onClick = onAddWallet) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.add_wallet_cta),
                    )
                }
                HomeTab.History -> FloatingActionButton(onClick = onRecordTransaction) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.record_tx_title),
                    )
                }
                HomeTab.Insights -> Unit
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (selectedTab) {
                HomeTab.Dashboard -> DashboardTab(
                    onRecordSale = onRecordSale,
                    onRecordExpense = onRecordExpense,
                    onAddWallet = onAddWallet,
                    onSeeAllWallets = { selectedTab = HomeTab.Wallets },
                    onOpenWallet = onOpenWallet,
                )
                HomeTab.Wallets -> WalletsTab(onOpenWallet = onOpenWallet)
                HomeTab.History -> HistoryTab()
                HomeTab.Insights -> InsightsTab()
            }
        }
    }
}

@Composable
private fun HomeBottomBar(
    selected: HomeTab,
    onSelect: (HomeTab) -> Unit,
) {
    NavigationBar {
        HomeTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(imageVector = tab.icon(), contentDescription = null) },
                label = { Text(text = stringResource(tab.labelRes())) },
            )
        }
    }
}

private fun HomeTab.icon(): ImageVector = when (this) {
    HomeTab.Dashboard -> Icons.Outlined.Home
    HomeTab.Wallets -> Icons.Outlined.AccountBalanceWallet
    HomeTab.Insights -> Icons.Outlined.Insights
    HomeTab.History -> Icons.AutoMirrored.Outlined.ReceiptLong
}

private fun HomeTab.labelRes(): Int = when (this) {
    HomeTab.Dashboard -> R.string.home_tab_dashboard
    HomeTab.Wallets -> R.string.home_tab_wallets
    HomeTab.Insights -> R.string.home_tab_insights
    HomeTab.History -> R.string.home_tab_history
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomePreview() {
    FlowtaTheme {
        HomeContent(
            uiState = HomeUiState.Content(
                businessName = "Mama Lucy Kiosk",
                currency = CurrencyCode.KES,
            ),
            onAddWallet = {},
            onRecordSale = {},
            onRecordExpense = {},
            onRecordTransaction = {},
            onOpenWallet = {},
        )
    }
}
