package com.flowgroup.flowta.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.license.ActivateLicenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val activateLicense: ActivateLicenseUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaywallUiState>(PaywallUiState.Idle)
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    fun onActivate(code: String) {
        if (code.isBlank()) {
            _uiState.value = PaywallUiState.Error("Enter your activation code")
            return
        }
        viewModelScope.launch {
            _uiState.value = PaywallUiState.Loading
            _uiState.value = when (activateLicense(code)) {
                is Result.Success -> PaywallUiState.Activated
                is Result.Error -> PaywallUiState.Error("Invalid code — check and try again")
            }
        }
    }

    sealed class PaywallUiState {
        data object Idle : PaywallUiState()
        data object Loading : PaywallUiState()
        data object Activated : PaywallUiState()
        data class Error(val message: String) : PaywallUiState()
    }
}
