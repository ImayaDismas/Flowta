package com.flowgroup.flowta.domain.usecase.deni

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.ClientDeni
import com.flowgroup.flowta.domain.repository.DeniRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObserveClientsDeniForCurrentBusinessUseCase @Inject constructor(
    private val deniRepository: DeniRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Result<List<ClientDeni>>> =
        preferencesRepository.currentBusinessId.flatMapLatest { id ->
            if (id == null) flowOf(Result.Success(emptyList()))
            else deniRepository.observeClientsWithBalance(id)
        }
}
