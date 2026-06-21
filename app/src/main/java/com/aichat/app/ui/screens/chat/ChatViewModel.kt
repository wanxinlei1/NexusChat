package com.aichat.app.ui.screens.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aichat.app.data.repository.ChatRepository
import com.aichat.app.data.repository.ChatRepository.CacheStats
import com.aichat.app.data.repository.ConversationRepository
import com.aichat.app.domain.model.ApiConfig
import com.aichat.app.domain.model.ChatMessage
import com.aichat.app.domain.model.Conversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val apiConfig: ApiConfig? = null,
    val providers: List<ApiConfig> = emptyList(),
    val selectedImageUri: String? = null,
    val cacheStats: CacheStats = CacheStats()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val conversationRepository: ConversationRepository,
    private val application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentConversationId: String? = null
    private var currentConversationCreatedAt: Long? = null

    init {
        loadConfig()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            combine(repository.apiConfig, repository.providers) { config, providers ->
                _uiState.value = _uiState.value.copy(
                    apiConfig = config,
                    providers = providers
                )
            }.collect { }
        }
    }

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun setSelectedImage(uri: String?) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri)
    }

    fun clearImage() {
        _uiState.value = _uiState.value.copy(selectedImageUri = null)
    }

    fun switchProvider(id: String) {
        viewModelScope.launch {
            repository.setActiveProvider(id)
        }
    }

    fun loadConversation(id: String) {
        viewModelScope.launch {
            val conv = conversationRepository.get(id) ?: return@launch
            currentConversationId = conv.id
            currentConversationCreatedAt = conv.createdAt
            _uiState.value = _uiState.value.copy(
                messages = conv.messages,
                isLoading = false,
                error = null,
                cacheStats = CacheStats()
            )
        }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        val imageUri = _uiState.value.selectedImageUri
        if (text.isBlank() && imageUri == null) return
        if (_uiState.value.isLoading) return

        val config = _uiState.value.apiConfig ?: return

        viewModelScope.launch {
            val userMessage = ChatMessage(
                content = text.ifBlank { " " },
                isUser = true,
                imageUri = imageUri
            )
            val updatedMessages = _uiState.value.messages + userMessage

            _uiState.value = _uiState.value.copy(
                messages = updatedMessages,
                inputText = "",
                selectedImageUri = null,
                isLoading = true,
                error = null
            )

            val result = repository.sendMessage(updatedMessages, config, application)

            result.fold(
                onSuccess = { response ->
                    val aiMessage = ChatMessage(
                        content = response.content,
                        isUser = false,
                        reasoningContent = response.reasoningContent,
                        promptTokens = response.promptTokens,
                        completionTokens = response.completionTokens,
                        totalTokens = response.totalTokens,
                        fromCache = response.fromCache
                    )
                    val newMessages = _uiState.value.messages + aiMessage
                    _uiState.value = _uiState.value.copy(
                        messages = newMessages,
                        isLoading = false,
                        cacheStats = repository.getCacheStats()
                    )
                    // Auto-save conversation after each exchange
                    autoSave(newMessages, config)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "\u53d1\u9001\u5931\u8d25"
                    )
                }
            )
        }
    }

    fun startNewConversation() {
        // Save current conversation, then start a fresh one
        val currentMessages = _uiState.value.messages
        val config = _uiState.value.apiConfig
        if (currentMessages.isNotEmpty() && config != null) {
            autoSave(currentMessages, config)
        }
        currentConversationId = null
        currentConversationCreatedAt = null
        repository.clearCacheStats()
        _uiState.value = _uiState.value.copy(messages = emptyList(), selectedImageUri = null, cacheStats = CacheStats())
    }

    fun clearMessages() {
        // Discard current conversation without saving
        currentConversationId = null
        currentConversationCreatedAt = null
        repository.clearCacheStats()
        _uiState.value = _uiState.value.copy(messages = emptyList(), selectedImageUri = null, cacheStats = CacheStats())
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun autoSave(messages: List<ChatMessage>, config: ApiConfig) {
        if (messages.isEmpty() || messages.all { it.isUser }) return

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val id = currentConversationId ?: java.util.UUID.randomUUID().toString().also {
                currentConversationId = it
                currentConversationCreatedAt = now
            }
            val title = conversationRepository.generateTitle(messages)
            val conversation = Conversation(
                id = id,
                title = title,
                model = config.model,
                endpoint = config.endpoint,
                createdAt = currentConversationCreatedAt ?: now,
                updatedAt = now,
                messages = messages
            )
            conversationRepository.save(conversation)
        }
    }
}

