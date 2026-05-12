package com.aichat.app.data.repository

import android.content.Context
import android.net.Uri
import com.aichat.app.data.local.SettingsDataStore
import com.aichat.app.data.remote.*
import com.aichat.app.domain.model.ApiConfig
import com.aichat.app.domain.model.ChatMessage
import com.aichat.app.util.ImageUtil
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
        config: ApiConfig,
        context: Context
    ): Result<String> {
        return try {
            val apiService = retrofitClient.createChatApiService(config.endpoint)

            // 构建消息列表：历史消息用纯文本，最后一条用户消息可能带图片
            val apiMessages = messages.mapIndexed { index, msg ->
                val isLastUserMessage = index == messages.lastIndex && msg.isUser
                if (isLastUserMessage && msg.imageUri != null) {
                    // 多模态消息
                    val parts = mutableListOf<ContentPart>()
                    if (msg.content.isNotBlank()) {
                        parts.add(ContentPart(type = "text", text = msg.content))
                    }
                    val base64 = ImageUtil.uriToBase64(context, Uri.parse(msg.imageUri))
                    parts.add(
                        ContentPart(
                            type = "image_url",
                            imageUrl = ImageUrl(url = base64)
                        )
                    )
                    Message(role = "user", content = parts)
                } else {
                    // 纯文本消息
                    val role = if (msg.isUser) "user" else "assistant"
                    Message(role = role, content = msg.content)
                }
            }

            val request = ChatRequest(
                model = config.model,
                messages = apiMessages
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
                messages = listOf(Message(role = "user", content = "Hi"))
            )
            apiService.chat("Bearer ${config.apiKey}", request)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
