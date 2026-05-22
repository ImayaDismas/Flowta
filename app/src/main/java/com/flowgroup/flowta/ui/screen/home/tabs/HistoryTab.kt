package com.flowgroup.flowta.ui.screen.home.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flowgroup.flowta.R
import com.flowgroup.flowta.ui.components.EmptyState

@Composable
fun HistoryTab(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
        title = stringResource(R.string.history_empty_title),
        message = stringResource(R.string.history_empty_message),
        modifier = modifier,
    )
}
