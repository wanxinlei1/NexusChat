package com.aichat.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aichat.app.data.local.ChatHistoryManager
import com.aichat.app.domain.model.ApiConfig
import com.aichat.app.domain.model.Message
import com.aichat.app.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val apiConfig: ApiConfig = ApiConfig(),
    val showConfigDialog: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val chatHistoryManager: ChatHistoryManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadApiConfig()
        observeMessages()
    }

    private fun loadApiConfig() {
        viewModelScope.launch {
            repository.getApiConfig().collect { config ->
                _uiState.update { it.copy(
                    apiConfig = config,
                    showConfigDialog = config.apiKey.isBlank()
                )}
            }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatHistoryManager.messages.collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || _uiState.value.isLoading) return

        val config = _uiState.value.apiConfig
        if (config.apiKey.isBlank()) {
            _uiState.update { it.copy(showConfigDialog = true) }
            return
        }

        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            content = content,
            isUser = true
        )

        viewModelScope.launch {
            chatHistoryManager.addMessage(userMessage)
            _uiState.update { it.copy(error = null) }

            val currentMessages = chatHistoryManager.getMessages()
            _uiState.update { it.copy(isLoading = true) }

            val result = repository.sendMessage(config, currentMessages)

            result.fold(
                onSuccess = { response ->
                    val aiMessage = Message(
                        id = UUID.randomUUID().toString(),
                        content = response,
                        isUser = false
                    )
                    chatHistoryManager.addMessage(aiMessage)
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error"
                    )}
                }
            )
        }
    }

    fun showConfigDialog() {
        _uiState.update { it.copy(showConfigDialog = true) }
    }

    fun hideConfigDialog() {
        _uiState.update { it.copy(showConfigDialog = false) }
    }

    fun saveApiConfig(config: ApiConfig) {
        viewModelScope.launch {
            repository.saveApiConfig(config)
            _uiState.update { it.copy(
                apiConfig = config,
                showConfigDialog = false
            )}
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatHistoryManager.clearMessages()
        }
    }
}
