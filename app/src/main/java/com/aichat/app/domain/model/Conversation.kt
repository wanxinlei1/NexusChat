package com.aichat.app.domain.model

data class Conversation(
    val id: String,
    val title: String,
    val model: String,
    val endpoint: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messages: List<ChatMessage> = emptyList()
) {
    val messageCount: Int get() = messages.size
    val previewText: String get() = messages.firstOrNull()?.content?.take(60) ?: ""
}
