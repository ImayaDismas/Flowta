package com.flowgroup.flowta.ui.screen.reconciliation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.ui.state.reconciliation.PasteSmsUiState
import com.flowgroup.flowta.ui.viewmodel.reconciliation.PasteSmsViewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun PasteSmsScreen(
    onBack: () -> Unit,
    onParsed: () -> Unit,
    viewModel: PasteSmsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.storedCount) {
        if ((uiState.storedCount ?: 0) > 0) onParsed()
    }

    PasteSmsContent(
        uiState = uiState,
        onBack = onBack,
        onInputChange = viewModel::onInputChange,
        onParse = viewModel::onParse,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasteSmsContent(
    uiState: PasteSmsUiState,
    onBack: () -> Unit,
    onInputChange: (String) -> Unit,
    onParse: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.paste_sms_title)) },
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.paste_sms_helper),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = uiState.input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                placeholder = { Text(stringResource(R.string.paste_sms_input_hint)) },
                enabled = !uiState.isParsing,
            )
            if (uiState.storedCount == 0) {
                Text(
                    text = stringResource(R.string.paste_sms_result_duplicate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (uiState.failed) {
                Text(
                    text = stringResource(R.string.paste_sms_error_unreadable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Button(
                onClick = onParse,
                enabled = uiState.canParse,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.paste_sms_parse_cta))
            }
            Text(
                text = stringResource(R.string.paste_sms_supported),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
