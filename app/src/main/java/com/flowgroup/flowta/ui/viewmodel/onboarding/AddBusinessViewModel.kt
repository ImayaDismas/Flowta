package com.flowgroup.flowta.ui.viewmodel.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.business.CreateBusinessUseCase
import com.flowgroup.flowta.ui.state.onboarding.AddBusinessEvent
import com.flowgroup.flowta.ui.state.onboarding.AddBusinessUiEvent
import com.flowgroup.flowta.ui.state.onboarding.AddBusinessUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBusinessViewModel @Inject constructor(
    private val createBusiness: CreateBusinessUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddBusinessUiState>(AddBusinessUiState.Content())
    val uiState: StateFlow<AddBusinessUiState> = _uiState.asStateFlow()

    private val _events = Channel<AddBusinessUiEvent>(Channel.BUFFERED)
    val events: Flow<AddBusinessUiEvent> = _events.receiveAsFlow()

    fun onEvent(event: AddBusinessEvent) {
        when (event) {
            is AddBusinessEvent.NameChanged -> updateContent {
                it.copy(name = event.name, nameError = null, submitError = null)
            }
            is AddBusinessEvent.CurrencyChanged -> updateContent {
                it.copy(currency = event.currency)
            }
            AddBusinessEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val current = (_uiState.value as? AddBusinessUiState.Content) ?: return
        if (current.isSubmitting) return

        val trimmed = current.name.trim()
        val nameError = when {
            trimmed.isBlank() -> AddBusinessUiState.Content.NameError.Blank
            trimmed.length > MAX_NAME_LENGTH -> AddBusinessUiState.Content.NameError.TooLong
            else -> null
        }
        if (nameError != null) {
            updateContent { it.copy(nameError = nameError) }
            return
        }

        updateContent { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            when (val result = createBusiness(trimmed, current.currency)) {
                is Result.Success -> {
                    updateContent { it.copy(isSubmitting = false) }
                    _events.send(AddBusinessUiEvent.NavigateNext)
                }
                is Result.Error -> updateContent {
                    it.copy(isSubmitting = false, submitError = result.exception.message)
                }
            }
        }
    }

    private inline fun updateContent(block: (AddBusinessUiState.Content) -> AddBusinessUiState.Content) {
        _uiState.update { state ->
            when (state) {
                is AddBusinessUiState.Content -> block(state)
            }
        }
    }

    private companion object { const val MAX_NAME_LENGTH = 80 }
}