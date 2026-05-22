package com.flowgroup.flowta.ui.state.unlock

sealed class PinUnlockUiState {
    data class Content(
        val enteredLength: Int = 0,
        val isVerifying: Boolean = false,
        val error: Error? = null,
    ) : PinUnlockUiState() {
        enum class Error { Incorrect, Unexpected }
    }

    companion object {
        const val PIN_LENGTH = 4
    }
}

sealed class PinUnlockEvent {
    data class DigitPressed(val digit: Int) : PinUnlockEvent()
    data object Backspace : PinUnlockEvent()
}

sealed class PinUnlockUiEvent {
    data object Unlocked : PinUnlockUiEvent()
}
