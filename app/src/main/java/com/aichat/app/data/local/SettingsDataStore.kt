package com.aichat.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aichat.app.domain.model.ApiConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val providersKey = stringPreferencesKey("providers")
    private val activeProviderIdKey = stringPreferencesKey("active_provider_id")
    // Legacy keys — for migration only
    private val legacyEndpointKey = stringPreferencesKey("api_endpoint")
    private val legacyApiKeyKey = stringPreferencesKey("api_key")
    private val legacyModelKey = stringPreferencesKey("model")

    /** All saved providers */
    val providers: Flow<List<ApiConfig>> = context.dataStore.data.map { preferences ->
        val json = preferences[providersKey]
        if (json.isNullOrBlank()) {
            // Migration: try reading legacy single-config and convert to list
            val legacyEndpoint = preferences[legacyEndpointKey]
            val legacyApiKey = preferences[legacyApiKeyKey]
            if (legacyEndpoint != null && legacyApiKey != null) {
                val legacyModel = preferences[legacyModelKey] ?: "gpt-3.5-turbo"
                listOf(
                    ApiConfig(
                        id = UUID.randomUUID().toString(),
                        name = "默认供应商",
                        endpoint = legacyEndpoint,
                        apiKey = legacyApiKey,
                        model = legacyModel
                    )
                )
            } else emptyList()
        } else {
            try {
                val type = object : TypeToken<List<ApiConfig>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    private val activeProviderId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[activeProviderIdKey]
    }

    /** The currently active provider, derived from [providers] + activeProviderId */
    val activeProvider: Flow<ApiConfig?> = combine(providers, activeProviderId) { list, activeId ->
        if (list.isEmpty()) null
        else list.find { it.id == activeId } ?: list.firstOrNull()
    }

    /** True when there are no saved providers */
    val isFirstLaunch: Flow<Boolean> = providers.map { it.isEmpty() }

    /** Save or update a provider. If [config.id] is empty, a new UUID is generated. */
    suspend fun saveProvider(config: ApiConfig) {
        context.dataStore.edit { preferences ->
            val json = preferences[providersKey]
            val type = object : TypeToken<MutableList<ApiConfig>>() {}.type
            val list: MutableList<ApiConfig> = if (json.isNullOrBlank()) {
                // Also run migration if needed
                val legacyEndpoint = preferences[legacyEndpointKey]
                val legacyApiKey = preferences[legacyApiKeyKey]
                if (legacyEndpoint != null && legacyApiKey != null) {
                    val legacyModel = preferences[legacyModelKey] ?: "gpt-3.5-turbo"
                    mutableListOf(
                        ApiConfig(
                            id = UUID.randomUUID().toString(),
                            name = "默认供应商",
                            endpoint = legacyEndpoint,
                            apiKey = legacyApiKey,
                            model = legacyModel
                        )
                    )
                } else mutableListOf()
            } else {
                try {
                    gson.fromJson(json, type) ?: mutableListOf()
                } catch (_: Exception) {
                    mutableListOf()
                }
            }

            if (config.id.isBlank()) {
                // New provider — assign UUID
                val newConfig = config.copy(id = UUID.randomUUID().toString())
                list.add(newConfig)
            } else {
                // Update existing
                val idx = list.indexOfFirst { it.id == config.id }
                if (idx >= 0) {
                    list[idx] = config
                } else {
                    list.add(config)
                }
            }

            // Set first provider as active if none is active
            val activeId = preferences[activeProviderIdKey]
            if (activeId == null && list.isNotEmpty()) {
                preferences[activeProviderIdKey] = list.first().id
            }

            // Clean up legacy keys after migration
            preferences.remove(legacyEndpointKey)
            preferences.remove(legacyApiKeyKey)
            preferences.remove(legacyModelKey)

            preferences[providersKey] = gson.toJson(list)
        }
    }

    /** Delete a provider by id. If it was the active provider, activate the next one. */
    suspend fun deleteProvider(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[providersKey] ?: return@edit
            val type = object : TypeToken<MutableList<ApiConfig>>() {}.type
            val list: MutableList<ApiConfig> = try {
                gson.fromJson(json, type) ?: return@edit
            } catch (_: Exception) {
                return@edit
            }

            list.removeAll { it.id == id }

            val activeId = preferences[activeProviderIdKey]
            if (activeId == id) {
                preferences[activeProviderIdKey] = list.firstOrNull()?.id ?: ""
            }

            preferences[providersKey] = gson.toJson(list)
        }
    }

    /** Set the active provider by id */
    suspend fun setActiveProvider(id: String) {
        context.dataStore.edit { preferences ->
            preferences[activeProviderIdKey] = id
        }
    }
}
