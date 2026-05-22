package com.flowgroup.flowta.ui.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.business.ObserveCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.home.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeCurrentBusiness: ObserveCurrentBusinessUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCurrentBusiness().collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> {
                        val business = result.data
                        if (business == null) HomeUiState.MissingBusiness
                        else HomeUiState.Content(
                            businessName = business.name,
                            currency = business.currency,
                        )
                    }
                    is Result.Error -> HomeUiState.MissingBusiness
                }
            }
        }
    }
}
