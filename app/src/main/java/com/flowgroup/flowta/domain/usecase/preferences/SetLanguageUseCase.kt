package com.flowgroup.flowta.domain.usecase.preferences

import com.flowgroup.flowta.domain.model.AppLanguage
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(language: AppLanguage) {
        preferencesRepository.setLanguage(language)
    }
}