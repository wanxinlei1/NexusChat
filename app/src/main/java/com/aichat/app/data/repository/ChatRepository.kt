package com.aichat.app.data.repository

import android.content.Context
import android.net.Uri
import com.aichat.app.data.local.SettingsDataStore
import com.aichat.app.data.remote.*
import com.aichat.app.domain.model.ApiConfig
import com.aichat.app.domain.model.ChatMessage
import com.aichat.app.util.ImageUtil
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val retrofitClient: RetrofitClient,
    private val settingsDataStore: SettingsDataStore
) {
    // ── Response cache ─────────────────────────────────────────
    private val responseCache = object : LinkedHashMap<String, SendResult>(100, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, SendResult>): Boolean = size > 100
    }
    private var cacheHits = 0
    private var cacheMisses = 0

    data class CacheStats(
        val hits: Int = 0,
        val misses: Int = 0
    ) {
        val hitRate: Float get() = if (hits + misses == 0) 0f else hits.toFloat() / (hits + misses)
        val totalRequests: Int get() = hits + misses
    }

    fun getCacheStats(): CacheStats = CacheStats(cacheHits, cacheMisses)

    fun clearCacheStats() {
        cacheHits = 0
        cacheMisses = 0
    }

    /** Invalidate the entire response cache */
    fun clearCache() {
        synchronized(responseCache) {
            responseCache.clear()
        }
    }

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
        val reasoningContent: String? = null,
        val promptTokens: Int = 0,
        val completionTokens: Int = 0,
        val totalTokens: Int = 0,
        val fromCache: Boolean = false
    )

    private fun buildCacheKey(messages: List<ChatMessage>, config: ApiConfig): String {
        val sb = StringBuilder()
        sb.append(config.endpoint).append("|")
        sb.append(config.model).append("|")
        for (msg in messages) {
            sb.append(if (msg.isUser) "u" else "a").append(":")
            sb.append(msg.content).append("|")
            if (msg.imageUri != null) {
                sb.append("img:").append(msg.imageUri).append("|")
            }
        }
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(sb.toString().toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    suspend fun sendMessage(
        messages: List<ChatMessage>,
        config: ApiConfig,
        context: Context
    ): Result<SendResult> {
        return try {
            val cacheKey = buildCacheKey(messages, config)

            // Check cache
            synchronized(responseCache) {
                val cached = responseCache[cacheKey]
                if (cached != null) {
                    cacheHits++
                    return Result.success(cached.copy(fromCache = true))
                }
            }
            cacheMisses++

            val apiService = retrofitClient.createChatApiService(config.endpoint)

            // Build message list: history uses plain text, last user message may have images
            val apiMessages = messages.mapIndexed { index, msg ->
                val isLastUserMessage = index == messages.lastIndex && msg.isUser
                if (isLastUserMessage && msg.imageUri != null) {
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
            val usage = response.usage

            val sendResult = SendResult(
                content = msg.content,
                reasoningContent = msg.reasoningContent,
                promptTokens = usage?.promptTokens ?: 0,
                completionTokens = usage?.completionTokens ?: 0,
                totalTokens = usage?.totalTokens ?: 0
            )

            synchronized(responseCache) {
                responseCache[cacheKey] = sendResult
            }

            Result.success(sendResult)
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

    suspend fun fetchModels(endpoint: String, apiKey: String): Result<List<String>> {
        return try {
            val apiService = retrofitClient.createChatApiService(endpoint)
            val response = apiService.listModels("Bearer $apiKey")
            Result.success(response.data.map { it.id }.sorted())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun validateApi(config: ApiConfig): Result<ValidationResult> {
        val stepErrors = mutableListOf<String>()
        var reachable = false
        var authorized = false
        var modelValid = false
        val availableModels = mutableListOf<String>()

        try {
            val apiService = retrofitClient.createChatApiService(config.endpoint)
            val modelsResponse = apiService.listModels("Bearer ${config.apiKey}")
            reachable = true
            availableModels.addAll(modelsResponse.data.map { it.id })
            authorized = true
            modelValid = availableModels.any { it.equals(config.model, ignoreCase = true) }
            if (!modelValid) {
                stepErrors.add("模型 ${config.model} 不在可用列表中")
            }
        } catch (e: retrofit2.HttpException) {
            reachable = true
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
