package com.flowgroup.flowta.ui.viewmodel.reconciliation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.PaymentSource
import com.flowgroup.flowta.domain.usecase.reconciliation.ParseAndStorePaymentsUseCase
import com.flowgroup.flowta.ui.state.reconciliation.PasteSmsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasteSmsViewModel @Inject constructor(
    private val parseAndStore: ParseAndStorePaymentsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasteSmsUiState())
    val uiState: StateFlow<PasteSmsUiState> = _uiState.asStateFlow()

    fun onInputChange(text: String) {
        _uiState.update { it.copy(input = text, storedCount = null, failed = false) }
    }

    fun onParse() {
        val text = _uiState.value.input
        if (text.isBlank() || _uiState.value.isParsing) return
        _uiState.update { it.copy(isParsing = true, storedCount = null, failed = false) }
        viewModelScope.launch {
            when (val result = parseAndStore(listOf(text), PaymentSource.SMS_PASTE)) {
                is Result.Success ->
                    _uiState.update { it.copy(isParsing = false, storedCount = result.data.stored) }
                is Result.Error ->
                    _uiState.update { it.copy(isParsing = false, failed = true) }
            }
        }
    }
}
