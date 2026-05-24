package com.flowgroup.flowta.domain.usecase.license

import com.flowgroup.flowta.domain.model.LicenseState
import com.flowgroup.flowta.domain.repository.LicenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLicenseStateUseCase @Inject constructor(
    private val repository: LicenseRepository,
) {
    operator fun invoke(): Flow<LicenseState> = repository.getLicenseState()
}
