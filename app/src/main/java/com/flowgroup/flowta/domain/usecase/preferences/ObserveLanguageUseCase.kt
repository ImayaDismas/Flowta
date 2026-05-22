package com.flowgroup.flowta.domain.usecase.preferences

import com.flowgroup.flowta.domain.model.AppLanguage
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLanguageUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) {
    operator fun invoke(): Flow<AppLanguage> = preferencesRepository.language
}