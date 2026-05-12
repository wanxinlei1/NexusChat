package com.aichat.app.domain.model

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null   // 本地图片 URI，非空时表示包含图片
)

data class ApiConfig(
    val endpoint: String,
    val apiKey: String,
    val model: String = "gpt-3.5-turbo"
)
