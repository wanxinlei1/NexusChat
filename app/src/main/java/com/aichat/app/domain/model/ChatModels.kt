package com.aichat.app.domain.model

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,              // 本地图片 URI
    val reasoningContent: String? = null,       // 思考过程（千问/DeepSeek 思考模型）
    val promptTokens: Int = 0,                  // 输入 Token 数
    val completionTokens: Int = 0,              // 输出 Token 数
    val totalTokens: Int = 0,                   // 总 Token 数
    val fromCache: Boolean = false              // 是否来自缓存
)

data class ApiConfig(
    val id: String = "",
    val name: String = "",
    val endpoint: String,
    val apiKey: String,
    val model: String = "gpt-3.5-turbo"
)
