package com.aichat.app.data.repository

import com.aichat.app.data.local.SettingsDataStore
import com.aichat.app.data.remote.ChatRequest
import com.aichat.app.data.remote.Message
import com.aichat.app.data.remote.RetrofitClient
import com.aichat.app.domain.model.ApiConfig
import com.aichat.app.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val retrofitClient: RetrofitClient,
    private val settingsDataStore: SettingsDataStore
) {
    val apiConfig: Flow<ApiConfig?> = settingsDataStore.apiConfig
    val isFirstLaunch: Flow<Boolean> = settingsDataStore.isFirstLaunch

    suspend fun saveApiConfig(config: ApiConfig) {
        settingsDataStore.saveApiConfig(config)
    }

    suspend fun sendMessage(
        messages: List<ChatMessage>,
        config: ApiConfig
    ): Result<String> {
        return try {
            val apiService = retrofitClient.createChatApiService(config.endpoint)
            val request = ChatRequest(
                model = config.model,
                messages = messages.map { Message("user", it.content) }
            )
            val response = apiService.chat("Bearer ${config.apiKey}", request)
            Result.success(response.choices.first().message.content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(config: ApiConfig): Result<Boolean> {
        return try {
            val apiService = retrofitClient.createChatApiService(config.endpoint)
            val request = ChatRequest(
                model = config.model,
                messages = listOf(Message("user", "Hi"))
            )
            apiService.chat("Bearer ${config.apiKey}", request)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
