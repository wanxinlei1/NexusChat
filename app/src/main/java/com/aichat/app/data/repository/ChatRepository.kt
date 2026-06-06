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
    /** All saved providers */
    val providers: Flow<List<ApiConfig>> = settingsDataStore.providers

    /** Currently active provider */
    val apiConfig: Flow<ApiConfig?> = settingsDataStore.activeProvider

    val isFirstLaunch: Flow<Boolean> = settingsDataStore.isFirstLaunch

    suspend fun saveProvider(config: ApiConfig) {
        settingsDataStore.saveProvider(config)
    }

    suspend fun deleteProvider(id: String) {
        settingsDataStore.deleteProvider(id)
    }

    suspend fun setActiveProvider(id: String) {
        settingsDataStore.setActiveProvider(id)
    }

    data class SendResult(
        val content: String,
        val reasoningContent: String? = null
    )

    suspend fun sendMessage(
        messages: List<ChatMessage>,
        config: ApiConfig,
        context: Context
    ): Result<SendResult> {
        return try {
            val apiService = retrofitClient.createChatApiService(config.endpoint)

            // Build message list: history uses plain text, last user message may have images
            val apiMessages = messages.mapIndexed { index, msg ->
                val isLastUserMessage = index == messages.lastIndex && msg.isUser
                if (isLastUserMessage && msg.imageUri != null) {
                    // Multimodal message
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
                    // Plain text message
                    val role = if (msg.isUser) "user" else "assistant"
                    Message(role = role, content = msg.content)
                }
            }

            val request = ChatRequest(
                model = config.model,
                messages = apiMessages
            )
            val response = apiService.chat("Bearer ${config.apiKey}", request)
            val msg = response.choices.first().message
            Result.success(
                SendResult(
                    content = msg.content,
                    reasoningContent = msg.reasoningContent
                )
            )
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

    /**
     * Fetch available model IDs from the API endpoint.
     */
    suspend fun fetchModels(endpoint: String, apiKey: String): Result<List<String>> {
        return try {
            val apiService = retrofitClient.createChatApiService(endpoint)
            val response = apiService.listModels("Bearer $apiKey")
            Result.success(response.data.map { it.id }.sorted())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Step-by-step API validation.
     * Step 1: Network reachability
     * Step 2: API key authorization
     * Step 3: Model availability
     */
    suspend fun validateApi(config: ApiConfig): Result<ValidationResult> {
        val stepErrors = mutableListOf<String>()
        var reachable = false
        var authorized = false
        var modelValid = false
        val availableModels = mutableListOf<String>()

        // Step 1: Check network reachability
        try {
            val apiService = retrofitClient.createChatApiService(config.endpoint)
            // Try fetching models as a lightweight connectivity check
            val modelsResponse = apiService.listModels("Bearer ${config.apiKey}")
            reachable = true
            availableModels.addAll(modelsResponse.data.map { it.id })

            // Step 2: Auth is implied by successful models fetch
            authorized = true

            // Step 3: Check if the configured model is available
            modelValid = availableModels.any { it.equals(config.model, ignoreCase = true) }
            if (!modelValid) {
                stepErrors.add("模型 \"${config.model}\" 不在可用列表中")
            }
        } catch (e: retrofit2.HttpException) {
            reachable = true // endpoint responded, so it's reachable
            val code = e.code()
            if (code == 401 || code == 403) {
                authorized = false
                stepErrors.add("API Key 无效（${code}）")
            } else {
                stepErrors.add("HTTP ${code}: ${e.message()}")
            }
        } catch (e: java.net.UnknownHostException) {
            reachable = false
            stepErrors.add("无法解析主机名")
        } catch (e: java.net.ConnectException) {
            reachable = false
            stepErrors.add("无法连接到服务器")
        } catch (e: java.net.SocketTimeoutException) {
            reachable = false
            stepErrors.add("连接超时")
        } catch (e: Exception) {
            if (!reachable) {
                stepErrors.add("网络不可达: ${e.message}")
            } else {
                stepErrors.add(e.message ?: "未知错误")
            }
        }

        return Result.success(
            ValidationResult(
                reachable = reachable,
                authorized = authorized,
                modelValid = modelValid,
                availableModels = availableModels,
                stepErrors = stepErrors,
                checkedModel = config.model
            )
        )
    }
}
