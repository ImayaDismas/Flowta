package com.flowgroup.flowta.ui.viewmodel.deni

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.deni.ObserveCustomersDeniForCurrentBusinessUseCase
import com.flowgroup.flowta.domain.usecase.deni.ObserveTotalOutstandingForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.deni.DeniListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeniListViewModel @Inject constructor(
    observeCustomers: ObserveCustomersDeniForCurrentBusinessUseCase,
    observeTotal: ObserveTotalOutstandingForCurrentBusinessUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeniListUiState>(DeniListUiState.Loading)
    val uiState: StateFlow<DeniListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(observeCustomers(), observeTotal()) { customersResult, totalResult ->
                when {
                    customersResult is Result.Error ->
                        DeniListUiState.Error(customersResult.exception.message.orEmpty())
                    totalResult is Result.Error ->
                        DeniListUiState.Error(totalResult.exception.message.orEmpty())
                    else -> {
                        val customers = (customersResult as Result.Success).data
                        val total = (totalResult as Result.Success).data
                        if (customers.isEmpty()) {
                            DeniListUiState.Empty
                        } else {
                            DeniListUiState.Content(
                                customers = customers,
                                totalOutstandingMinor = total,
                                currency = customers.first().customer.currency,
                            )
                        }
                    }
                }
            }.collect { _uiState.value = it }
        }
    }
}
