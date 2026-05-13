package com.aichat.app.ui.screens.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aichat.app.data.repository.ChatRepository
import com.aichat.app.domain.model.ApiConfig
import com.aichat.app.domain.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val apiConfig: ApiConfig? = null,
    val selectedImageUri: String? = null   // 当前选中的图片 URI
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            repository.apiConfig.collect { config ->
                _uiState.value = _uiState.value.copy(apiConfig = config)
            }
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

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        val imageUri = _uiState.value.selectedImageUri
        if (text.isBlank() && imageUri == null) return
        if (_uiState.value.isLoading) return

        val config = _uiState.value.apiConfig ?: return

        viewModelScope.launch {
            val userMessage = ChatMessage(
                content = text.ifBlank { " " },  // 纯图片时用空格占位
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
                        reasoningContent = response.reasoningContent
                    )
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + aiMessage,
                        isLoading = false
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "发送失败"
                    )
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(messages = emptyList(), selectedImageUri = null)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
