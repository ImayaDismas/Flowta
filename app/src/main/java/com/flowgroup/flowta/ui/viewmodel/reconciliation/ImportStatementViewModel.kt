package com.flowgroup.flowta.ui.viewmodel.reconciliation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.reconciliation.ImportStatementUseCase
import com.flowgroup.flowta.ui.state.reconciliation.ImportStatementUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportStatementViewModel @Inject constructor(
    private val importStatement: ImportStatementUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportStatementUiState())
    val uiState: StateFlow<ImportStatementUiState> = _uiState.asStateFlow()

    fun onFileSelected(fileName: String, csvText: String) {
        _uiState.update {
            it.copy(isImporting = true, fileName = fileName, storedCount = null, errorMessage = null)
        }
        viewModelScope.launch {
            when (val result = importStatement(csvText)) {
                is Result.Success ->
                    _uiState.update { it.copy(isImporting = false, storedCount = result.data.stored) }
                is Result.Error ->
                    _uiState.update { it.copy(isImporting = false, errorMessage = result.exception.message) }
            }
        }
    }

    fun onFileReadFailed() {
        _uiState.update { it.copy(isImporting = false, errorMessage = "Could not read the file") }
    }
}
