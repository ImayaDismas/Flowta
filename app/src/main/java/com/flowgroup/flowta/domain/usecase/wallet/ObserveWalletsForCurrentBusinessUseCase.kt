package com.flowgroup.flowta.domain.usecase.wallet

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Wallet
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import com.flowgroup.flowta.domain.repository.WalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObserveWalletsForCurrentBusinessUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Result<List<Wallet>>> =
        preferencesRepository.currentBusinessId.flatMapLatest { id ->
            if (id == null) flowOf(Result.Success(emptyList()))
            else walletRepository.observeForBusiness(id)
        }
}
