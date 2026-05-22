package com.flowgroup.flowta.ui.screen.unlock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.ui.components.Numpad
import com.flowgroup.flowta.ui.components.PinDots
import com.flowgroup.flowta.ui.state.unlock.PinUnlockEvent
import com.flowgroup.flowta.ui.state.unlock.PinUnlockUiEvent
import com.flowgroup.flowta.ui.state.unlock.PinUnlockUiState
import com.flowgroup.flowta.ui.theme.FlowtaTheme
import com.flowgroup.flowta.ui.viewmodel.unlock.PinUnlockViewModel

@Composable
fun PinUnlockScreen(
    onUnlocked: () -> Unit,
    viewModel: PinUnlockViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                PinUnlockUiEvent.Unlocked -> onUnlocked()
            }
        }
    }

    PinUnlockContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun PinUnlockContent(
    uiState: PinUnlockUiState,
    onEvent: (PinUnlockEvent) -> Unit,
) {
    val content = uiState as PinUnlockUiState.Content

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.pin_unlock_title),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.pin_unlock_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.weight(0.2f))

        PinDots(filled = content.enteredLength, total = PinUnlockUiState.PIN_LENGTH)

        content.error?.let { error ->
            val resId = when (error) {
                PinUnlockUiState.Content.Error.Incorrect -> R.string.pin_unlock_incorrect
                PinUnlockUiState.Content.Error.Unexpected -> R.string.pin_unlock_unexpected
            }
            Text(
                text = stringResource(resId),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.weight(0.5f))

        Numpad(
            onDigit = { onEvent(PinUnlockEvent.DigitPressed(it)) },
            onBackspace = { onEvent(PinUnlockEvent.Backspace) },
            enabled = !content.isVerifying,
        )

        Spacer(Modifier.weight(0.2f))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PinUnlockPreview() {
    FlowtaTheme {
        PinUnlockContent(
            uiState = PinUnlockUiState.Content(enteredLength = 2),
            onEvent = {},
        )
    }
}
