package com.flowgroup.flowta.domain.usecase.wallet

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.repository.TransactionRepository
import com.flowgroup.flowta.domain.repository.WalletRepository
import javax.inject.Inject

class DeleteWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(id: String): Outcome {
        return when (val countResult = transactionRepository.countForWallet(id)) {
            is Result.Error -> Outcome.Failed(countResult.exception.message.orEmpty())
            is Result.Success -> {
                if (countResult.data > 0) {
                    Outcome.Blocked(countResult.data)
                } else {
                    when (val deleteResult = walletRepository.deleteById(id)) {
                        is Result.Success -> Outcome.Deleted
                        is Result.Error -> Outcome.Failed(deleteResult.exception.message.orEmpty())
                    }
                }
            }
        }
    }

    sealed class Outcome {
        data object Deleted : Outcome()
        data class Blocked(val transactionCount: Int) : Outcome()
        data class Failed(val message: String) : Outcome()
    }
}
