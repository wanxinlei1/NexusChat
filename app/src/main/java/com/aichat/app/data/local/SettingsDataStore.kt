package com.aichat.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aichat.app.domain.model.ApiConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val endpointKey = stringPreferencesKey("api_endpoint")
    private val apiKeyKey = stringPreferencesKey("api_key")
    private val modelKey = stringPreferencesKey("model")
    private val firstLaunchKey = booleanPreferencesKey("first_launch")

    val apiConfig: Flow<ApiConfig?> = context.dataStore.data.map { preferences ->
        val endpoint = preferences[endpointKey]
        val apiKey = preferences[apiKeyKey]
        if (endpoint != null && apiKey != null) {
            ApiConfig(
                endpoint = endpoint,
                apiKey = apiKey,
                model = preferences[modelKey] ?: "gpt-3.5-turbo"
            )
        } else null
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[firstLaunchKey] ?: true
    }

    suspend fun saveApiConfig(config: ApiConfig) {
        context.dataStore.edit { preferences ->
            preferences[endpointKey] = config.endpoint
            preferences[apiKeyKey] = config.apiKey
            preferences[modelKey] = config.model
            preferences[firstLaunchKey] = false
        }
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[firstLaunchKey] = false
        }
    }
}
