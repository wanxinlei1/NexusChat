package com.aichat.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aichat.app.data.repository.ChatRepository
import com.aichat.app.domain.model.ApiConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditingProviderState(
    val id: String = "",             // empty = new, non-empty = editing existing
    val name: String = "",
    val endpoint: String = "",
    val apiKey: String = "",
    val model: String = "gpt-3.5-turbo"
)

data class SettingsUiState(
    val providers: List<ApiConfig> = emptyList(),
    val activeProviderId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val testSuccess: Boolean? = null,
    val isSaved: Boolean = false,
    // Dialog state: non-null = show edit dialog
    val editingProvider: EditingProviderState? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProviders()
    }

    private fun loadProviders() {
        viewModelScope.launch {
            combine(repository.providers, repository.apiConfig) { providers, activeConfig ->
                _uiState.value = _uiState.value.copy(
                    providers = providers,
                    activeProviderId = activeConfig?.id
                )
            }.collect { }
        }
    }

    // ── Dialog helpers ──────────────────────────────────

    fun startAddProvider() {
        _uiState.value = _uiState.value.copy(
            editingProvider = EditingProviderState(),
            testSuccess = null,
            error = null
        )
    }

    fun startEditProvider(config: ApiConfig) {
        _uiState.value = _uiState.value.copy(
            editingProvider = EditingProviderState(
                id = config.id,
                name = config.name,
                endpoint = config.endpoint,
                apiKey = config.apiKey,
                model = config.model
            ),
            testSuccess = null,
            error = null
        )
    }

    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(
            editingProvider = null,
            testSuccess = null,
            error = null
        )
    }

    // ── Editing fields ──────────────────────────────────

    fun updateEditingName(name: String) {
        _uiState.value = _uiState.value.copy(
            editingProvider = _uiState.value.editingProvider?.copy(name = name),
            error = null
        )
    }

    fun updateEditingEndpoint(endpoint: String) {
        _uiState.value = _uiState.value.copy(
            editingProvider = _uiState.value.editingProvider?.copy(endpoint = endpoint),
            error = null
        )
    }

    fun updateEditingApiKey(apiKey: String) {
        _uiState.value = _uiState.value.copy(
            editingProvider = _uiState.value.editingProvider?.copy(apiKey = apiKey),
            error = null
        )
    }

    fun updateEditingModel(model: String) {
        _uiState.value = _uiState.value.copy(
            editingProvider = _uiState.value.editingProvider?.copy(model = model)
        )
    }

    // ── CRUD ────────────────────────────────────────────

    fun saveProvider() {
        val edit = _uiState.value.editingProvider ?: return
        if (edit.name.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "请输入供应商名称")
            return
        }
        if (edit.endpoint.isBlank() || edit.apiKey.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "请填写 API Endpoint 和 API Key")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val config = ApiConfig(
                id = edit.id,
                name = edit.name.trim(),
                endpoint = edit.endpoint.trim(),
                apiKey = edit.apiKey.trim(),
                model = edit.model.ifBlank { "gpt-3.5-turbo" }
            )
            repository.saveProvider(config)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                editingProvider = null,
                isSaved = true
            )
        }
    }

    fun deleteProvider(id: String) {
        viewModelScope.launch {
            repository.deleteProvider(id)
        }
    }

    fun setActiveProvider(id: String) {
        viewModelScope.launch {
            repository.setActiveProvider(id)
        }
    }

    // ── Presets ─────────────────────────────────────────

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
        val current = _uiState.value.editingProvider ?: return
        _uiState.value = _uiState.value.copy(
            editingProvider = current.copy(
                endpoint = preset.endpoint,
                model = preset.model
            ),
            error = null
        )
    }

    // ── Test Connection ─────────────────────────────────

    fun testConnection() {
        val edit = _uiState.value.editingProvider ?: return
        if (edit.endpoint.isBlank() || edit.apiKey.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "请填写 API Endpoint 和 API Key")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, testSuccess = null)
            val config = ApiConfig(
                endpoint = edit.endpoint.trim(),
                apiKey = edit.apiKey.trim(),
                model = edit.model.ifBlank { "gpt-3.5-turbo" }
            )
            val result = repository.testConnection(config)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                testSuccess = result.isSuccess,
                error = result.exceptionOrNull()?.message
            )
        }
    }

    fun markSavedConsumed() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}
