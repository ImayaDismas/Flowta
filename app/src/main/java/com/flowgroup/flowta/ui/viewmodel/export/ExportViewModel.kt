package com.flowgroup.flowta.ui.viewmodel.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.transaction.ExportTransactionsCsvUseCase
import com.flowgroup.flowta.ui.state.export.ExportUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportTransactionsCsv: ExportTransactionsCsvUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun onExport() {
        if (_uiState.value.isPreparing) return
        _uiState.update {
            it.copy(isPreparing = true, content = null, savedOk = false, empty = false, errorMessage = null)
        }
        viewModelScope.launch {
            when (val result = exportTransactionsCsv()) {
                is Result.Success -> {
                    val export = result.data
                    if (export.rowCount == 0) {
                        _uiState.update { it.copy(isPreparing = false, empty = true) }
                    } else {
                        _uiState.update {
                            it.copy(isPreparing = false, content = export.content, rowCount = export.rowCount)
                        }
                    }
                }
                is Result.Error ->
                    _uiState.update { it.copy(isPreparing = false, errorMessage = result.exception.message) }
            }
        }
    }

    /** Called after the CSV has been written to the chosen file. */
    fun onSaved() {
        _uiState.update { it.copy(content = null, savedOk = true) }
    }

    fun onSaveFailed() {
        _uiState.update { it.copy(content = null, errorMessage = "Could not save the file") }
    }
}
