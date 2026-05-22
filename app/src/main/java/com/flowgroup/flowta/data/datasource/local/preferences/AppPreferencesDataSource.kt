package com.flowgroup.flowta.data.datasource.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.flowgroup.flowta.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val language: Flow<AppLanguage> = dataStore.data.map { prefs ->
        AppLanguage.fromCode(prefs[Keys.LANGUAGE])
    }

    val currentBusinessId: Flow<String?> = dataStore.data.map { it[Keys.CURRENT_BUSINESS_ID] }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setCurrentBusinessId(id: String?) {
        dataStore.edit { prefs ->
            if (id == null) prefs.remove(Keys.CURRENT_BUSINESS_ID)
            else prefs[Keys.CURRENT_BUSINESS_ID] = id
        }
    }

    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val CURRENT_BUSINESS_ID = stringPreferencesKey("current_business_id")
    }
}
