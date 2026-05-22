package com.flowgroup.flowta.domain.usecase.business

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Business
import com.flowgroup.flowta.domain.repository.BusinessRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObserveCurrentBusinessUseCase @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Result<Business?>> =
        preferencesRepository.currentBusinessId.flatMapLatest { id ->
            if (id == null) flowOf(Result.Success(null))
            else businessRepository.observeById(id)
        }
}