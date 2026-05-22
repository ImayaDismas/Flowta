package com.flowgroup.flowta.domain.repository

import com.flowgroup.flowta.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val language: Flow<AppLanguage>
    val currentBusinessId: Flow<String?>
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setCurrentBusinessId(id: String?)
}
