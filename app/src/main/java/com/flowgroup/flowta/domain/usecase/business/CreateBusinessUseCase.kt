package com.flowgroup.flowta.domain.usecase.business

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.Business
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.repository.BusinessRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import javax.inject.Inject

class CreateBusinessUseCase @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(name: String, currency: CurrencyCode): Result<Business> {
        val result = businessRepository.create(name, currency)
        if (result is Result.Success) {
            preferencesRepository.setCurrentBusinessId(result.data.id)
        }
        return result
    }
}