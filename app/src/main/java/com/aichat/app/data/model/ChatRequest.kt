package com.aichat.app.data.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String,
    val messages: List<MessageRequest>,
    @SerializedName("max_tokens")
    val maxTokens: Int = 2000,
    val stream: Boolean = false
)

data class MessageRequest(
    val role: String,
    val content: String
)
