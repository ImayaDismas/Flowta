package com.flowgroup.flowta.ui.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.wallet.ObserveWalletsForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.home.WalletsTabUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletsTabViewModel @Inject constructor(
    observeWallets: ObserveWalletsForCurrentBusinessUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WalletsTabUiState>(WalletsTabUiState.Loading)
    val uiState: StateFlow<WalletsTabUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeWallets().collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> WalletsTabUiState.Content(result.data)
                    is Result.Error -> WalletsTabUiState.Error(result.exception.message.orEmpty())
                }
            }
        }
    }
}
