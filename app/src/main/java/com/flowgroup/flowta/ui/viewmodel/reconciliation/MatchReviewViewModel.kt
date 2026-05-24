package com.flowgroup.flowta.ui.viewmodel.reconciliation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.usecase.reconciliation.ConfirmMatchUseCase
import com.flowgroup.flowta.domain.usecase.reconciliation.GetMatchableTransactionsUseCase
import com.flowgroup.flowta.domain.usecase.reconciliation.GetReceivedPaymentUseCase
import com.flowgroup.flowta.domain.usecase.reconciliation.IgnorePaymentUseCase
import com.flowgroup.flowta.domain.usecase.reconciliation.RecordTransactionFromPaymentUseCase
import com.flowgroup.flowta.domain.usecase.reconciliation.SuggestMatchUseCase
import com.flowgroup.flowta.domain.usecase.wallet.ObserveWalletsWithBalanceForCurrentBusinessUseCase
import com.flowgroup.flowta.ui.state.reconciliation.MatchReviewUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPayment: GetReceivedPaymentUseCase,
    private val suggestMatch: SuggestMatchUseCase,
    private val confirmMatch: ConfirmMatchUseCase,
    private val ignorePayment: IgnorePaymentUseCase,
    private val recordTransaction: RecordTransactionFromPaymentUseCase,
    private val observeWallets: ObserveWalletsWithBalanceForCurrentBusinessUseCase,
    private val getMatchableTransactions: GetMatchableTransactionsUseCase,
) : ViewModel() {

    private val paymentId: String = checkNotNull(savedStateHandle.get<String>("paymentId"))

    private val _uiState = MutableStateFlow<MatchReviewUiState>(MatchReviewUiState.Loading)
    val uiState: StateFlow<MatchReviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val payment = when (val r = getPayment(paymentId)) {
                is Result.Success -> r.data
                is Result.Error -> {
                    _uiState.value = MatchReviewUiState.Error(r.exception.message.orEmpty())
                    return@launch
                }
            }
            if (payment == null) {
                _uiState.value = MatchReviewUiState.Error("Payment not found")
                return@launch
            }
            val suggestion = (suggestMatch(payment) as? Result.Success)?.data
            val wallets = (observeWallets().first() as? Result.Success)?.data ?: emptyList()
            val defaultWallet = wallets.firstOrNull { it.wallet.type == payment.provider.toWalletType() }
                ?: wallets.firstOrNull()
            _uiState.value = MatchReviewUiState.Content(
                payment = payment,
                suggestion = suggestion,
                showSuggestion = suggestion != null,
                wallets = wallets,
                selectedWalletId = defaultWallet?.wallet?.id,
            )
        }
    }

    fun onSelectWallet(walletId: String) {
        _uiState.update { state ->
            (state as? MatchReviewUiState.Content)?.copy(selectedWalletId = walletId) ?: state
        }
    }

    fun onNotAMatch() {
        _uiState.update { state ->
            (state as? MatchReviewUiState.Content)?.copy(showSuggestion = false) ?: state
        }
    }

    fun onPickDifferent() {
        val content = _uiState.value as? MatchReviewUiState.Content ?: return
        viewModelScope.launch {
            val transactions = (getMatchableTransactions(content.payment) as? Result.Success)?.data
                ?: emptyList()
            _uiState.update { state ->
                (state as? MatchReviewUiState.Content)?.copy(
                    pickableTransactions = transactions,
                    showTransactionPicker = true,
                ) ?: state
            }
        }
    }

    fun onSelectDifferentMatch(transactionId: String) {
        _uiState.update { state ->
            (state as? MatchReviewUiState.Content)?.copy(showTransactionPicker = false) ?: state
        }
        runWorking { confirmMatch(paymentId, transactionId) }
    }

    fun onCancelPicker() {
        _uiState.update { state ->
            (state as? MatchReviewUiState.Content)?.copy(showTransactionPicker = false) ?: state
        }
    }

    fun onConfirmMatch() {
        val content = _uiState.value as? MatchReviewUiState.Content ?: return
        val transactionId = content.suggestion?.id ?: return
        runWorking { confirmMatch(paymentId, transactionId) }
    }

    fun onRecordTransaction() {
        val content = _uiState.value as? MatchReviewUiState.Content ?: return
        val walletId = content.selectedWalletId ?: return
        runWorking { recordTransaction(paymentId, walletId) }
    }

    fun onDismiss() {
        if (_uiState.value !is MatchReviewUiState.Content) return
        runWorking { ignorePayment(paymentId) }
    }

    private inline fun runWorking(crossinline action: suspend () -> Result<Unit>) {
        _uiState.update { state ->
            (state as? MatchReviewUiState.Content)?.copy(working = true) ?: state
        }
        viewModelScope.launch {
            when (val result = action()) {
                is Result.Success -> _uiState.value = MatchReviewUiState.Done
                is Result.Error -> _uiState.value = MatchReviewUiState.Error(result.exception.message.orEmpty())
            }
        }
    }
}

private fun MobileMoneyProvider.toWalletType(): WalletType = when (this) {
    MobileMoneyProvider.MPESA -> WalletType.MPESA
    MobileMoneyProvider.AIRTEL_MONEY -> WalletType.AIRTEL_MONEY
    MobileMoneyProvider.TKASH -> WalletType.T_KASH
}
