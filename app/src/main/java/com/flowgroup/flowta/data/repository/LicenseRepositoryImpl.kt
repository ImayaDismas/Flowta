package com.flowgroup.flowta.data.repository

import com.flowgroup.flowta.data.datasource.local.LicenseCodeValidator
import com.flowgroup.flowta.data.datasource.local.LicenseLocalDataSource
import com.flowgroup.flowta.data.datasource.local.preferences.AppPreferencesDataSource
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.LicenseState
import com.flowgroup.flowta.domain.repository.LicenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LicenseRepositoryImpl @Inject constructor(
    private val licenseDataSource: LicenseLocalDataSource,
    private val preferencesDataSource: AppPreferencesDataSource,
    private val clock: Clock,
) : LicenseRepository {

    override fun getLicenseState(): Flow<LicenseState> = combine(
        licenseDataSource.isActivated,
        licenseDataSource.trialStartEpoch,
    ) { activated, trialStart ->
        when {
            activated -> LicenseState.Active
            trialStart == null -> LicenseState.Trial(TRIAL_DAYS)
            else -> {
                val elapsedDays = ((clock.now().toEpochMilliseconds() - trialStart) / MS_PER_DAY).toInt()
                val remaining = TRIAL_DAYS - elapsedDays
                if (remaining > 0) LicenseState.Trial(remaining) else LicenseState.Expired
            }
        }
    }

    override suspend fun initTrialIfNeeded() {
        licenseDataSource.initTrial(clock.now().toEpochMilliseconds())
    }

    override suspend fun activate(code: String): Result<Unit> {
        val businessId = preferencesDataSource.currentBusinessId.first()
            ?: return Result.Error(AppException.LocalException("No business registered"))
        return if (LicenseCodeValidator.verify(businessId, code)) {
            licenseDataSource.setActivated()
            Result.Success(Unit)
        } else {
            Result.Error(AppException.LocalException("Invalid activation code"))
        }
    }

    private companion object {
        const val TRIAL_DAYS = 30
        const val MS_PER_DAY = 1000L * 60 * 60 * 24
    }
}
