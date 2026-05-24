package com.flowgroup.flowta.ui.screen.reconciliation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flowgroup.flowta.R
import com.flowgroup.flowta.ui.components.EmptyState

/**
 * Placeholder entry screens for reconciliation methods 2–4 (camera OCR, statement import, SMS
 * inbox scan). Each will feed the same parse→match pipeline as paste-SMS once implemented; for now
 * they explain the method and reassure on privacy.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComingSoonScaffold(title: String, icon: ImageVector, message: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            EmptyState(icon = icon, title = title, message = message)
            Text(
                text = stringResource(R.string.reconciliation_coming_soon),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
fun ScanReceiptScreen(onBack: () -> Unit) = ComingSoonScaffold(
    title = stringResource(R.string.scan_receipt_title),
    icon = Icons.Outlined.PhotoCamera,
    message = stringResource(R.string.scan_receipt_message),
    onBack = onBack,
)

@Composable
fun ScanInboxScreen(onBack: () -> Unit) = ComingSoonScaffold(
    title = stringResource(R.string.scan_inbox_title),
    icon = Icons.Outlined.Inbox,
    message = stringResource(R.string.scan_inbox_message),
    onBack = onBack,
)
