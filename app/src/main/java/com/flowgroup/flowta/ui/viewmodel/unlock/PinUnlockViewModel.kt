package com.flowgroup.flowta.ui.viewmodel.unlock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.pin.VerifyPinUseCase
import com.flowgroup.flowta.ui.state.unlock.PinUnlockEvent
import com.flowgroup.flowta.ui.state.unlock.PinUnlockUiEvent
import com.flowgroup.flowta.ui.state.unlock.PinUnlockUiState
import com.flowgroup.flowta.ui.state.unlock.PinUnlockUiState.Companion.PIN_LENGTH
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
class PinUnlockViewModel @Inject constructor(
    private val verifyPin: VerifyPinUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PinUnlockUiState>(PinUnlockUiState.Content())
    val uiState: StateFlow<PinUnlockUiState> = _uiState.asStateFlow()

    private val _events = Channel<PinUnlockUiEvent>(Channel.BUFFERED)
    val events: Flow<PinUnlockUiEvent> = _events.receiveAsFlow()

    private val entered = CharArray(PIN_LENGTH)
    private var enteredLength = 0

    fun onEvent(event: PinUnlockEvent) {
        when (event) {
            is PinUnlockEvent.DigitPressed -> handleDigit(event.digit)
            PinUnlockEvent.Backspace -> handleBackspace()
        }
    }

    private fun handleDigit(digit: Int) {
        val state = currentContent() ?: return
        if (state.isVerifying) return
        if (enteredLength >= PIN_LENGTH) return

        entered[enteredLength] = ('0' + digit)
        enteredLength++
        updateContent { it.copy(enteredLength = enteredLength, error = null) }

        if (enteredLength == PIN_LENGTH) submit()
    }

    private fun handleBackspace() {
        val state = currentContent() ?: return
        if (state.isVerifying) return
        if (enteredLength == 0) return
        enteredLength--
        entered[enteredLength] = ' '
        updateContent { it.copy(enteredLength = enteredLength, error = null) }
    }

    private fun submit() {
        updateContent { it.copy(isVerifying = true, error = null) }
        val snapshot = entered.copyOf()
        viewModelScope.launch {
            when (val result = verifyPin(snapshot)) {
                is Result.Success -> {
                    if (result.data) {
                        clearEntered()
                        updateContent { it.copy(enteredLength = 0, isVerifying = false) }
                        _events.send(PinUnlockUiEvent.Unlocked)
                    } else {
                        clearEntered()
                        updateContent {
                            it.copy(
                                enteredLength = 0,
                                isVerifying = false,
                                error = PinUnlockUiState.Content.Error.Incorrect,
                            )
                        }
                    }
                }
                is Result.Error -> {
                    clearEntered()
                    updateContent {
                        it.copy(
                            enteredLength = 0,
                            isVerifying = false,
                            error = PinUnlockUiState.Content.Error.Unexpected,
                        )
                    }
                }
            }
        }
    }

    private fun clearEntered() {
        entered.fill(' ')
        enteredLength = 0
    }

    private fun currentContent(): PinUnlockUiState.Content? =
        _uiState.value as? PinUnlockUiState.Content

    private inline fun updateContent(block: (PinUnlockUiState.Content) -> PinUnlockUiState.Content) {
        _uiState.update { state ->
            when (state) {
                is PinUnlockUiState.Content -> block(state)
            }
        }
    }

    override fun onCleared() {
        entered.fill(' ')
        super.onCleared()
    }
}
