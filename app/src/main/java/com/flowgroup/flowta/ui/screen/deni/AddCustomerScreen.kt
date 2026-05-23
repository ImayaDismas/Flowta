package com.flowgroup.flowta.ui.screen.deni

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowgroup.flowta.R
import com.flowgroup.flowta.ui.state.deni.AddCustomerEvent
import com.flowgroup.flowta.ui.state.deni.AddCustomerUiEvent
import com.flowgroup.flowta.ui.state.deni.AddCustomerUiState
import com.flowgroup.flowta.ui.viewmodel.deni.AddCustomerViewModel

@Composable
fun AddCustomerScreen(
    onClose: () -> Unit,
    onCreated: () -> Unit,
    viewModel: AddCustomerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AddCustomerUiEvent.Saved -> onCreated()
            }
        }
    }

    AddCustomerContent(
        uiState = uiState,
        onClose = onClose,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomerContent(
    uiState: AddCustomerUiState,
    onClose: () -> Unit,
    onEvent: (AddCustomerEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_customer_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { onEvent(AddCustomerEvent.NameChanged(it)) },
                label = { Text(stringResource(R.string.add_customer_name_label)) },
                placeholder = { Text(stringResource(R.string.add_customer_name_hint)) },
                singleLine = true,
                isError = uiState.nameBlankError,
                supportingText = if (uiState.nameBlankError) {
                    { Text(stringResource(R.string.add_customer_error_name_blank), color = MaterialTheme.colorScheme.error) }
                } else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = uiState.phone,
                onValueChange = { onEvent(AddCustomerEvent.PhoneChanged(it)) },
                label = { Text(stringResource(R.string.add_customer_phone_label)) },
                placeholder = { Text(stringResource(R.string.add_customer_phone_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = uiState.initialCreditInput,
                onValueChange = { onEvent(AddCustomerEvent.InitialCreditChanged(it)) },
                label = { Text(stringResource(R.string.add_customer_initial_credit_label)) },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            uiState.submitError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onEvent(AddCustomerEvent.Save) },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.add_customer_save),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
