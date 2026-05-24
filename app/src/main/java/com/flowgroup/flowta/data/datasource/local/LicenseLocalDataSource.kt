package com.flowgroup.flowta.data.datasource.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LicenseLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val trialStartEpoch: Flow<Long?> = dataStore.data.map { it[Keys.TRIAL_START_EPOCH] }
    val isActivated: Flow<Boolean> = dataStore.data.map { it[Keys.IS_ACTIVATED] ?: false }

    suspend fun initTrial(epochMillis: Long) {
        dataStore.edit { prefs ->
            if (prefs[Keys.TRIAL_START_EPOCH] == null) {
                prefs[Keys.TRIAL_START_EPOCH] = epochMillis
            }
        }
    }

    suspend fun setActivated() {
        dataStore.edit { it[Keys.IS_ACTIVATED] = true }
    }

    private object Keys {
        val TRIAL_START_EPOCH = longPreferencesKey("trial_start_epoch")
        val IS_ACTIVATED = booleanPreferencesKey("license_activated")
    }
}
