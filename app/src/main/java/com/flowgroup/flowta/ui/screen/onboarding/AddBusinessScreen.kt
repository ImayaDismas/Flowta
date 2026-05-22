package com.flowgroup.flowta.ui.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.ui.state.onboarding.AddBusinessEvent
import com.flowgroup.flowta.ui.state.onboarding.AddBusinessUiEvent
import com.flowgroup.flowta.ui.state.onboarding.AddBusinessUiState
import com.flowgroup.flowta.ui.theme.FlowtaTheme
import com.flowgroup.flowta.ui.viewmodel.onboarding.AddBusinessViewModel

private val SupportedCurrencies = listOf(
    CurrencyCode.KES,
    CurrencyCode.UGX,
    CurrencyCode.TZS,
    CurrencyCode.RWF,
    CurrencyCode.USD,
)

@Composable
fun AddBusinessScreen(
    onNext: () -> Unit,
    viewModel: AddBusinessViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AddBusinessUiEvent.NavigateNext -> onNext()
            }
        }
    }

    AddBusinessContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun AddBusinessContent(
    uiState: AddBusinessUiState,
    onEvent: (AddBusinessEvent) -> Unit,
) {
    val content = uiState as AddBusinessUiState.Content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.add_business_title),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(R.string.add_business_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedTextField(
            value = content.name,
            onValueChange = { onEvent(AddBusinessEvent.NameChanged(it)) },
            label = { Text(stringResource(R.string.add_business_name_label)) },
            placeholder = { Text(stringResource(R.string.add_business_name_hint)) },
            singleLine = true,
            isError = content.nameError != null,
            supportingText = content.nameError?.let { error ->
                { Text(stringResource(error.messageRes), color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )

        CurrencyDropdown(
            selected = content.currency,
            onSelect = { onEvent(AddBusinessEvent.CurrencyChanged(it)) },
        )

        content.submitError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { onEvent(AddBusinessEvent.Submit) },
            enabled = !content.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            if (content.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = stringResource(R.string.common_continue),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    selected: CurrencyCode,
    onSelect: (CurrencyCode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selected.label(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.add_business_currency_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            SupportedCurrencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency.label()) },
                    onClick = {
                        onSelect(currency)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun CurrencyCode.label(): String = when (iso4217) {
    "KES" -> stringResource(R.string.currency_kes)
    "UGX" -> stringResource(R.string.currency_ugx)
    "TZS" -> stringResource(R.string.currency_tzs)
    "RWF" -> stringResource(R.string.currency_rwf)
    "USD" -> stringResource(R.string.currency_usd)
    else -> iso4217
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AddBusinessPreview() {
    FlowtaTheme {
        AddBusinessContent(
            uiState = AddBusinessUiState.Content(name = "Mama Lucy Kiosk"),
            onEvent = {},
        )
    }
}
