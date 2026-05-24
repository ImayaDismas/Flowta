package com.flowgroup.flowta.domain.usecase.license

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.repository.LicenseRepository
import javax.inject.Inject

class ActivateLicenseUseCase @Inject constructor(
    private val repository: LicenseRepository,
) {
    suspend operator fun invoke(code: String): Result<Unit> = repository.activate(code)
}
