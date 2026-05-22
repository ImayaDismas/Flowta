package com.flowgroup.flowta.data.repository

import com.flowgroup.flowta.data.datasource.local.preferences.AppPreferencesDataSource
import com.flowgroup.flowta.domain.model.AppLanguage
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataSource: AppPreferencesDataSource,
) : PreferencesRepository {
    override val language: Flow<AppLanguage> = dataSource.language
    override val currentBusinessId: Flow<String?> = dataSource.currentBusinessId
    override suspend fun setLanguage(language: AppLanguage) = dataSource.setLanguage(language)
    override suspend fun setCurrentBusinessId(id: String?) = dataSource.setCurrentBusinessId(id)
}