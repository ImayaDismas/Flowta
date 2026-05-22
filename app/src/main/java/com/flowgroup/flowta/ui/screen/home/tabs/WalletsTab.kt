package com.flowgroup.flowta.ui.screen.home.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flowgroup.flowta.R
import com.flowgroup.flowta.ui.components.EmptyState

@Composable
fun WalletsTab(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.AccountBalanceWallet,
        title = stringResource(R.string.wallets_empty_title),
        message = stringResource(R.string.wallets_empty_message),
        modifier = modifier,
    )
}
