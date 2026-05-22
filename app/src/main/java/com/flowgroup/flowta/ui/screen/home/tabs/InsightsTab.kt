package com.flowgroup.flowta.ui.screen.home.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flowgroup.flowta.R
import com.flowgroup.flowta.ui.components.EmptyState

@Composable
fun InsightsTab(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.Insights,
        title = stringResource(R.string.insights_empty_title),
        message = stringResource(R.string.insights_empty_message),
        modifier = modifier,
    )
}
