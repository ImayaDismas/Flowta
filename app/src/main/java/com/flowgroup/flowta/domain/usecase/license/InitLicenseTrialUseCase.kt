package com.flowgroup.flowta.domain.usecase.license

import com.flowgroup.flowta.domain.repository.LicenseRepository
import javax.inject.Inject

class InitLicenseTrialUseCase @Inject constructor(
    private val repository: LicenseRepository,
) {
    suspend operator fun invoke() = repository.initTrialIfNeeded()
}
