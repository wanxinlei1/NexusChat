package com.aichat.app.data.repository

import com.aichat.app.data.api.ChatApiService
import com.aichat.app.data.local.ChatHistoryManager
import com.aichat.app.data.local.SettingsDataStore
import com.aichat.app.data.model.ChatRequest
import com.aichat.app.data.model.MessageRequest
import com.aichat.app.domain.model.ApiConfig
import com.aichat.app.domain.model.Message
import com.aichat.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val apiService: ChatApiService,
    private val settingsDataStore: SettingsDataStore,
    private val chatHistoryManager: ChatHistoryManager
) : ChatRepository {

    override suspend fun sendMessage(config: ApiConfig, messages: List<Message>): Result<String> {
        return try {
            val request = ChatRequest(
                model = config.model,
                messages = messages.map { MessageRequest(
                    role = if (it.isUser) "user" else "assistant",
                    content = it.content
                )}
            )

            val response = apiService.sendMessage(
                apiKey = "Bearer ${config.apiKey}",
                request = request
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.choices.isNotEmpty()) {
                    Result.success(body.choices.first().message.content)
                } else {
                    Result.failure(Exception("Empty response from API"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Invalid API key"
                    429 -> "Rate limit exceeded"
                    500 -> "Server error"
                    else -> "Error: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    override fun getApiConfig(): Flow<ApiConfig> = settingsDataStore.apiConfig

    override suspend fun saveApiConfig(config: ApiConfig) {
        settingsDataStore.saveApiConfig(config)
    }

    override suspend fun clearChatHistory() {
        chatHistoryManager.clearMessages()
    }

    override fun getChatHistory(): Flow<List<Message>> = chatHistoryManager.messages
}
