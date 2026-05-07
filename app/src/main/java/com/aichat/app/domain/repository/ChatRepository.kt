package com.aichat.app.domain.repository

import com.aichat.app.domain.model.ApiConfig
import com.aichat.app.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(config: ApiConfig, messages: List<Message>): Result<String>
    fun getApiConfig(): Flow<ApiConfig>
    suspend fun saveApiConfig(config: ApiConfig)
    suspend fun clearChatHistory()
    fun getChatHistory(): Flow<List<Message>>
}
