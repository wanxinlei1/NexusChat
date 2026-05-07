package com.aichat.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
    private val dataStore = context.dataStore

    companion object {
        private val API_KEY = stringPreferencesKey("api_key")
        private val BASE_URL = stringPreferencesKey("base_url")
        private val MODEL = stringPreferencesKey("model")
    }

    val apiConfig: Flow<ApiConfig> = dataStore.data.map { preferences ->
        ApiConfig(
            apiKey = preferences[API_KEY] ?: "",
            baseUrl = preferences[BASE_URL] ?: "https://api.openai.com/v1/",
            model = preferences[MODEL] ?: "gpt-3.5-turbo"
        )
    }

    suspend fun saveApiConfig(config: ApiConfig) {
        dataStore.edit { preferences ->
            preferences[API_KEY] = config.apiKey
            preferences[BASE_URL] = config.baseUrl
            preferences[MODEL] = config.model
        }
    }

    suspend fun clearApiConfig() {
        dataStore.edit { preferences ->
            preferences.remove(API_KEY)
            preferences.remove(BASE_URL)
            preferences.remove(MODEL)
        }
    }
}
