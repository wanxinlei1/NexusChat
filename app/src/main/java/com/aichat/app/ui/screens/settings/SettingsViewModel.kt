package com.aichat.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aichat.app.data.repository.ChatRepository
import com.aichat.app.domain.model.ApiConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val endpoint: String = "",
    val apiKey: String = "",
    val model: String = "gpt-3.5-turbo",
    val isLoading: Boolean = false,
    val error: String? = null,
    val testSuccess: Boolean? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            repository.apiConfig.collect { config ->
                config?.let {
                    _uiState.value = _uiState.value.copy(
                        endpoint = it.endpoint,
                        apiKey = it.apiKey,
                        model = it.model
                    )
                }
            }
        }
    }

    fun updateEndpoint(endpoint: String) {
        _uiState.value = _uiState.value.copy(endpoint = endpoint, error = null)
    }

    fun updateApiKey(apiKey: String) {
        _uiState.value = _uiState.value.copy(apiKey = apiKey, error = null)
    }

    fun updateModel(model: String) {
        _uiState.value = _uiState.value.copy(model = model)
    }

    fun saveConfig() {
        val state = _uiState.value
        if (state.endpoint.isBlank() || state.apiKey.isBlank()) {
            _uiState.value = state.copy(error = "请填写完整信息")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val config = ApiConfig(
                endpoint = state.endpoint.trim(),
                apiKey = state.apiKey.trim(),
                model = state.model.ifBlank { "gpt-3.5-turbo" }
            )
            repository.saveApiConfig(config)
            _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
        }
    }

    enum class ProviderPreset(
        val label: String,
        val endpoint: String,
        val model: String
    ) {
        QWEN("千问", "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus"),
        DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1", "deepseek-chat"),
        OPENAI("OpenAI", "https://api.openai.com/v1", "gpt-3.5-turbo")
    }

    fun applyPreset(preset: ProviderPreset) {
        _uiState.value = _uiState.value.copy(
            endpoint = preset.endpoint,
            model = preset.model,
            error = null
        )
    }

    fun testConnection() {
        val state = _uiState.value
        if (state.endpoint.isBlank() || state.apiKey.isBlank()) {
            _uiState.value = state.copy(error = "请填写完整信息")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null, testSuccess = null)
            val config = ApiConfig(
                endpoint = state.endpoint.trim(),
                apiKey = state.apiKey.trim(),
                model = state.model.ifBlank { "gpt-3.5-turbo" }
            )
            val result = repository.testConnection(config)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                testSuccess = result.isSuccess,
                error = result.exceptionOrNull()?.message
            )
        }
    }
}
