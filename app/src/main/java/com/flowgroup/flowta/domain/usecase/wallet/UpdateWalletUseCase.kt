package com.flowgroup.flowta.domain.usecase.wallet

import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.WalletType
import com.flowgroup.flowta.domain.repository.WalletRepository
import javax.inject.Inject

class UpdateWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
) {
    suspend operator fun invoke(
        id: String,
        name: String,
        type: WalletType,
    ): Result<Unit> {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return Result.Error(AppException.LocalException("Name cannot be blank"))
        }
        if (trimmed.length > MAX_NAME_LENGTH) {
            return Result.Error(AppException.LocalException("Name too long"))
        }
        return walletRepository.update(id = id, name = trimmed, type = type)
    }

    private companion object { const val MAX_NAME_LENGTH = 80 }
}
