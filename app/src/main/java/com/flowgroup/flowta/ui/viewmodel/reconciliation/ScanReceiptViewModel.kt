package com.flowgroup.flowta.ui.viewmodel.reconciliation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.PaymentSource
import com.flowgroup.flowta.domain.reconciliation.ReceiptTextRecognizer
import com.flowgroup.flowta.domain.usecase.reconciliation.ParseAndStorePaymentsUseCase
import com.flowgroup.flowta.ui.state.reconciliation.ScanReceiptUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanReceiptViewModel @Inject constructor(
    private val recognizer: ReceiptTextRecognizer,
    private val parseAndStore: ParseAndStorePaymentsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanReceiptUiState())
    val uiState: StateFlow<ScanReceiptUiState> = _uiState.asStateFlow()

    fun onImagePicked(imageUri: String) {
        if (_uiState.value.isProcessing) return
        _uiState.update { it.copy(isProcessing = true, storedCount = null, failed = false) }
        viewModelScope.launch {
            when (val recognized = recognizer.recognize(imageUri)) {
                is Result.Error -> _uiState.update { it.copy(isProcessing = false, failed = true) }
                is Result.Success -> store(recognized.data)
            }
        }
    }

    private suspend fun store(text: String) {
        when (val result = parseAndStore(listOf(text), PaymentSource.CAMERA_OCR)) {
            is Result.Success ->
                _uiState.update { it.copy(isProcessing = false, storedCount = result.data.stored) }
            is Result.Error ->
                _uiState.update { it.copy(isProcessing = false, failed = true) }
        }
    }

    fun onPickFailed() {
        _uiState.update { it.copy(isProcessing = false, failed = true) }
    }
}
