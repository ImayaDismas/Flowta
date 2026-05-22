package com.flowgroup.flowta.domain.usecase.business

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Onboarding is "complete" when the user has a current business set in preferences.
 * Used by the splash route to decide whether to send the user to Home or Get Started.
 */
class IsOnboardingCompleteUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(): Result<Boolean> = Result.Success(
        preferencesRepository.currentBusinessId.firstOrNull() != null
    )
}