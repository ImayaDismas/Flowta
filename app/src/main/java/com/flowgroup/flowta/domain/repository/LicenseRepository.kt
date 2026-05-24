package com.flowgroup.flowta.domain.repository

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.LicenseState
import kotlinx.coroutines.flow.Flow

interface LicenseRepository {
    fun getLicenseState(): Flow<LicenseState>
    suspend fun initTrialIfNeeded()
    suspend fun activate(code: String): Result<Unit>
}
