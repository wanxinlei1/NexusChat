package com.aichat.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aichat.app.data.remote.ValidationResult
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
    val id: String = "",
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
    // Dialog state
    val editingProvider: EditingProviderState? = null,
    // Validation
    val availableModels: List<String> = emptyList(),
    val isFetchingModels: Boolean = false,
    val validationResult: ValidationResult? = null,
    val isValidationInProgress: Boolean = false,
    val keyFormatHint: String? = null,
    val endpointFormatHint: String? = null
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
            error = null,
            availableModels = emptyList(),
            validationResult = null,
            keyFormatHint = null,
            endpointFormatHint = null
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
            error = null,
            availableModels = emptyList(),
            validationResult = null,
            keyFormatHint = checkKeyFormat(config.apiKey),
            endpointFormatHint = checkEndpointFormat(config.endpoint)
        )
    }

    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(
            editingProvider = null,
            testSuccess = null,
            error = null,
            availableModels = emptyList(),
            validationResult = null,
            keyFormatHint = null,
            endpointFormatHint = null
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
            error = null,
            availableModels = emptyList(),
            endpointFormatHint = checkEndpointFormat(endpoint)
        )
    }

    fun updateEditingApiKey(apiKey: String) {
        _uiState.value = _uiState.value.copy(
            editingProvider = _uiState.value.editingProvider?.copy(apiKey = apiKey),
            error = null,
            keyFormatHint = checkKeyFormat(apiKey)
        )
    }

    fun updateEditingModel(model: String) {
        _uiState.value = _uiState.value.copy(
            editingProvider = _uiState.value.editingProvider?.copy(model = model)
        )
    }

    // ── Model list ──────────────────────────────────────

    fun fetchModels() {
        val edit = _uiState.value.editingProvider ?: return
        if (edit.endpoint.isBlank() || edit.apiKey.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "请先填写 Endpoint 和 API Key")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFetchingModels = true, error = null)
            val result = repository.fetchModels(
                endpoint = edit.endpoint.trim(),
                apiKey = edit.apiKey.trim()
            )
            result.fold(
                onSuccess = { models ->
                    _uiState.value = _uiState.value.copy(
                        isFetchingModels = false,
                        availableModels = models
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isFetchingModels = false,
                        error = "获取模型列表失败: ${e.message}"
                    )
                }
            )
        }
    }

    fun selectModel(model: String) {
        _uiState.value = _uiState.value.copy(
            editingProvider = _uiState.value.editingProvider?.copy(model = model)
        )
    }

    // ── Key format check ────────────────────────────────

    /**
     * Returns a hint string about the key format, or null if format looks OK.
     */
    private fun checkKeyFormat(key: String): String? {
        if (key.isBlank()) return null
        return when {
            key.startsWith("sk-") && key.length >= 20 -> null // looks valid
            key.startsWith("sk-") && key.length < 20 -> "Key 似乎太短"
            key.contains(" ") -> "Key 不应包含空格"
            key.length < 10 -> "Key 格式似乎不正确"
            else -> null // unknown format, no warning
        }
    }

    /**
     * Returns a hint string about the endpoint format, or null if format looks OK.
     */
    private fun checkEndpointFormat(url: String): String? {
        if (url.isBlank()) return null
        val trimmed = url.trim()
        return when {
            !trimmed.startsWith("http://") && !trimmed.startsWith("https://") ->
                "URL 需要以 http:// 或 https:// 开头"
            trimmed.startsWith("http://") ->
                "建议使用 HTTPS 加密连接"
            !Regex("^https://[a-zA-Z0-9][-a-zA-Z0-9.]*\\.[a-zA-Z]{2,}").containsMatchIn(trimmed) ->
                "URL 格式似乎不正确"
            trimmed.contains(" ") ->
                "URL 不应包含空格"
            else -> null // looks OK
        }
    }

    // ── Step-by-step validation ─────────────────────────

    fun validateApi() {
        val edit = _uiState.value.editingProvider ?: return
        if (edit.endpoint.isBlank() || edit.apiKey.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "请先填写 Endpoint 和 API Key")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isValidationInProgress = true, error = null, validationResult = null)
            val config = ApiConfig(
                endpoint = edit.endpoint.trim(),
                apiKey = edit.apiKey.trim(),
                model = edit.model.ifBlank { "gpt-3.5-turbo" }
            )
            val result = repository.validateApi(config)
            result.fold(
                onSuccess = { vr ->
                    _uiState.value = _uiState.value.copy(
                        isValidationInProgress = false,
                        validationResult = vr,
                        availableModels = vr.availableModels,
                        testSuccess = vr.reachable && vr.authorized
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isValidationInProgress = false,
                        error = "检测失败: ${e.message}"
                    )
                }
            )
        }
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
                isSaved = true,
                validationResult = null,
                availableModels = emptyList()
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
            error = null,
            availableModels = emptyList(),
            validationResult = null
        )
    }

    // ── Legacy: simple connection test ──────────────────

    fun testConnection() {
        validateApi() // delegate to enhanced validation
    }

    fun markSavedConsumed() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}
