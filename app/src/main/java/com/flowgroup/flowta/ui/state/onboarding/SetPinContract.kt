package com.flowgroup.flowta.ui.state.onboarding

sealed class SetPinUiState {
    data class Content(
        val phase: Phase = Phase.Enter,
        val enteredLength: Int = 0,
        val confirmLength: Int = 0,
        val isSubmitting: Boolean = false,
        val mismatch: Boolean = false,
        val submitError: String? = null,
    ) : SetPinUiState() {
        enum class Phase { Enter, Confirm }
    }

    companion object {
        const val PIN_LENGTH = 4
    }
}

sealed class SetPinEvent {
    data class DigitPressed(val digit: Int) : SetPinEvent()
    data object Backspace : SetPinEvent()
    data object Restart : SetPinEvent()
}

sealed class SetPinUiEvent {
    data object NavigateNext : SetPinUiEvent()
}