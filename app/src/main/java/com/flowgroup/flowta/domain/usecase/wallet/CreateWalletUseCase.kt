package com.flowgroup.flowta.domain.usecase.wallet

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Money
import com.flowgroup.flowta.domain.model.Wallet
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.WalletRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CreateWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(
        name: String,
        type: WalletType,
        openingBalance: Money,
    ): Result<Wallet> {
        val businessId = preferencesRepository.currentBusinessId.firstOrNull()
            ?: return Result.Error(AppException.LocalException("No current business selected"))
        return walletRepository.create(businessId, name, type, openingBalance)
    }
}
