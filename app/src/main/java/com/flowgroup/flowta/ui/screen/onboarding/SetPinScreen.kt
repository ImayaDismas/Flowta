package com.flowgroup.flowta.ui.screen.onboarding

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
import com.flowgroup.flowta.ui.state.onboarding.SetPinEvent
import com.flowgroup.flowta.ui.state.onboarding.SetPinUiEvent
import com.flowgroup.flowta.ui.state.onboarding.SetPinUiState
import com.flowgroup.flowta.ui.theme.FlowtaTheme
import com.flowgroup.flowta.ui.viewmodel.onboarding.SetPinViewModel

@Composable
fun SetPinScreen(
    onNext: () -> Unit,
    viewModel: SetPinViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SetPinUiEvent.NavigateNext -> onNext()
            }
        }
    }

    SetPinContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun SetPinContent(
    uiState: SetPinUiState,
    onEvent: (SetPinEvent) -> Unit,
) {
    val content = uiState as SetPinUiState.Content
    val isConfirm = content.phase == SetPinUiState.Content.Phase.Confirm
    val filled = if (isConfirm) content.confirmLength else content.enteredLength

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
                text = stringResource(
                    if (isConfirm) R.string.set_pin_title_confirm else R.string.set_pin_title_enter
                ),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.set_pin_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.weight(0.2f))

        PinDots(filled = filled, total = SetPinUiState.PIN_LENGTH)

        if (content.mismatch) {
            Text(
                text = stringResource(R.string.set_pin_mismatch),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        content.submitError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.weight(0.5f))

        Numpad(
            onDigit = { onEvent(SetPinEvent.DigitPressed(it)) },
            onBackspace = { onEvent(SetPinEvent.Backspace) },
            enabled = !content.isSubmitting,
        )

        Spacer(Modifier.weight(0.2f))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SetPinPreview() {
    FlowtaTheme {
        SetPinContent(
            uiState = SetPinUiState.Content(enteredLength = 2),
            onEvent = {},
        )
    }
}