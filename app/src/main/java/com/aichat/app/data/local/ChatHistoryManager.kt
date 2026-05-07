package com.aichat.app.data.local

import com.aichat.app.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatHistoryManager @Inject constructor() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: Flow<List<Message>> = _messages.asStateFlow()

    fun addMessage(message: Message) {
        _messages.value = _messages.value + message
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun getMessages(): List<Message> = _messages.value
}
