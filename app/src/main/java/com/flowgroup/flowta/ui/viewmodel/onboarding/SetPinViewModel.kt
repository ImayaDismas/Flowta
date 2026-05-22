package com.flowgroup.flowta.ui.viewmodel.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.pin.SetPinUseCase
import com.flowgroup.flowta.ui.state.onboarding.SetPinEvent
import com.flowgroup.flowta.ui.state.onboarding.SetPinUiEvent
import com.flowgroup.flowta.ui.state.onboarding.SetPinUiState
import com.flowgroup.flowta.ui.state.onboarding.SetPinUiState.Companion.PIN_LENGTH
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
class SetPinViewModel @Inject constructor(
    private val setPin: SetPinUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SetPinUiState>(SetPinUiState.Content())
    val uiState: StateFlow<SetPinUiState> = _uiState.asStateFlow()

    private val _events = Channel<SetPinUiEvent>(Channel.BUFFERED)
    val events: Flow<SetPinUiEvent> = _events.receiveAsFlow()

    private val enteredPin = CharArray(PIN_LENGTH)
    private val confirmPin = CharArray(PIN_LENGTH)
    private var enteredLength = 0
    private var confirmLength = 0

    fun onEvent(event: SetPinEvent) {
        when (event) {
            is SetPinEvent.DigitPressed -> handleDigit(event.digit)
            SetPinEvent.Backspace -> handleBackspace()
            SetPinEvent.Restart -> reset()
        }
    }

    private fun handleDigit(digit: Int) {
        val state = currentContent() ?: return
        if (state.isSubmitting) return
        val buffer = bufferFor(state.phase)
        val length = lengthFor(state.phase)
        if (length >= PIN_LENGTH) return

        buffer[length] = ('0' + digit)
        incrementLength(state.phase)
        publishLengths()

        if (lengthFor(state.phase) == PIN_LENGTH) advancePhaseOrSubmit()
    }

    private fun handleBackspace() {
        val state = currentContent() ?: return
        if (state.isSubmitting) return
        val length = lengthFor(state.phase)
        if (length == 0) return
        bufferFor(state.phase)[length - 1] = ' '
        decrementLength(state.phase)
        updateContent { it.copy(mismatch = false, submitError = null) }
        publishLengths()
    }

    private fun reset() {
        enteredPin.fill(' ')
        confirmPin.fill(' ')
        enteredLength = 0
        confirmLength = 0
        _uiState.value = SetPinUiState.Content()
    }

    private fun advancePhaseOrSubmit() {
        val state = currentContent() ?: return
        when (state.phase) {
            SetPinUiState.Content.Phase.Enter -> {
                updateContent { it.copy(phase = SetPinUiState.Content.Phase.Confirm, mismatch = false) }
            }
            SetPinUiState.Content.Phase.Confirm -> {
                if (!enteredPin.contentEquals(confirmPin)) {
                    confirmPin.fill(' ')
                    confirmLength = 0
                    updateContent { it.copy(confirmLength = 0, mismatch = true) }
                    return
                }
                persistPin()
            }
        }
    }

    private fun persistPin() {
        updateContent { it.copy(isSubmitting = true, submitError = null) }
        val snapshot = enteredPin.copyOf()
        viewModelScope.launch {
            when (val result = setPin(snapshot)) {
                is Result.Success -> {
                    updateContent { it.copy(isSubmitting = false) }
                    _events.send(SetPinUiEvent.NavigateNext)
                }
                is Result.Error -> updateContent {
                    it.copy(isSubmitting = false, submitError = result.exception.message)
                }
            }
        }
    }

    private fun publishLengths() {
        updateContent { it.copy(enteredLength = enteredLength, confirmLength = confirmLength) }
    }

    private fun bufferFor(phase: SetPinUiState.Content.Phase): CharArray =
        if (phase == SetPinUiState.Content.Phase.Enter) enteredPin else confirmPin

    private fun lengthFor(phase: SetPinUiState.Content.Phase): Int =
        if (phase == SetPinUiState.Content.Phase.Enter) enteredLength else confirmLength

    private fun incrementLength(phase: SetPinUiState.Content.Phase) {
        if (phase == SetPinUiState.Content.Phase.Enter) enteredLength++ else confirmLength++
    }

    private fun decrementLength(phase: SetPinUiState.Content.Phase) {
        if (phase == SetPinUiState.Content.Phase.Enter) enteredLength-- else confirmLength--
    }

    private fun currentContent(): SetPinUiState.Content? = _uiState.value as? SetPinUiState.Content

    private inline fun updateContent(block: (SetPinUiState.Content) -> SetPinUiState.Content) {
        _uiState.update { state ->
            when (state) {
                is SetPinUiState.Content -> block(state)
            }
        }
    }

    override fun onCleared() {
        enteredPin.fill(' ')
        confirmPin.fill(' ')
        super.onCleared()
    }
}