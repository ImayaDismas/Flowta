package com.flowgroup.flowta.ui.viewmodel.wallet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.wallet.ObserveWalletDetailUseCase
import com.flowgroup.flowta.ui.state.wallet.WalletDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeWalletDetail: ObserveWalletDetailUseCase,
) : ViewModel() {

    val walletId: String = checkNotNull(savedStateHandle.get<String>("walletId"))

    private val _uiState = MutableStateFlow<WalletDetailUiState>(WalletDetailUiState.Loading)
    val uiState: StateFlow<WalletDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeWalletDetail(walletId).collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> result.data?.let { WalletDetailUiState.Content(it) }
                        ?: WalletDetailUiState.NotFound
                    is Result.Error -> WalletDetailUiState.Error(result.exception.message.orEmpty())
                }
            }
        }
    }
}
