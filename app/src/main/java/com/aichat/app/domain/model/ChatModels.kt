package com.aichat.app.domain.model

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ApiConfig(
    val endpoint: String,
    val apiKey: String,
    val model: String = "gpt-3.5-turbo"
)
