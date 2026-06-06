package com.aichat.app.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.aichat.app.data.remote.ValidationResult
import com.aichat.app.domain.model.ApiConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToChat: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cs = MaterialTheme.colorScheme
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.markSavedConsumed()
        }
    }

    Scaffold(
        containerColor = cs.background,
        topBar = {
            TopAppBar(
                title = { Text("⚙️ 模型供应商", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.surface,
                    titleContentColor = cs.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.providers.isEmpty()) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔌", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "还没有配置供应商",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = cs.onBackground
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "点击下方按钮添加第一个 API 供应商",
                            fontSize = 14.sp,
                            color = cs.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.providers, key = { it.id }) { provider ->
                        val isActive = provider.id == uiState.activeProviderId
                        ProviderCard(
                            provider = provider,
                            isActive = isActive,
                            onActivate = { viewModel.setActiveProvider(provider.id) },
                            onEdit = { viewModel.startEditProvider(provider) },
                            onDelete = { viewModel.deleteProvider(provider.id) }
                        )
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = viewModel::startAddProvider,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("添加供应商")
                }
                if (uiState.providers.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onNavigateToChat,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("完成")
                    }
                }
            }
        }
    }

    // ── Edit Dialog ─────────────────────────────────────
    uiState.editingProvider?.let { edit ->
        val isEditingExisting = edit.id.isNotBlank()
        ProviderEditDialog(
            editingProvider = edit,
            isLoading = uiState.isLoading,
            error = uiState.error,
            validationResult = uiState.validationResult,
            isValidationInProgress = uiState.isValidationInProgress,
            availableModels = uiState.availableModels,
            isFetchingModels = uiState.isFetchingModels,
            keyFormatHint = uiState.keyFormatHint,
            endpointFormatHint = uiState.endpointFormatHint,
            passwordVisible = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible },
            onUpdateName = viewModel::updateEditingName,
            onUpdateEndpoint = viewModel::updateEditingEndpoint,
            onUpdateApiKey = viewModel::updateEditingApiKey,
            onUpdateModel = viewModel::updateEditingModel,
            onSelectModel = viewModel::selectModel,
            onFetchModels = viewModel::fetchModels,
            onApplyPreset = viewModel::applyPreset,
            onValidateApi = viewModel::validateApi,
            onSave = viewModel::saveProvider,
            onCancel = viewModel::cancelEdit,
            isEditingExisting = isEditingExisting
        )
    }
}

// ── Provider Card ───────────────────────────────────────

@Composable
private fun ProviderCard(
    provider: ApiConfig,
    isActive: Boolean,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val cardColor = if (isActive) cs.primaryContainer else cs.surface
    val borderMod = if (isActive) {
        Modifier
            .background(cs.primary, RoundedCornerShape(12.dp))
            .padding(1.5.dp)
    } else Modifier

    Card(
        Modifier
            .fillMaxWidth()
            .then(borderMod)
            .clickable(onClick = onActivate),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = if (isActive) "当前使用" else "点击启用",
                tint = if (isActive) cs.primary else cs.outlineVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        provider.name.ifBlank { "未命名" },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = cs.onSurface
                    )
                    if (isActive) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "当前",
                            fontSize = 11.sp,
                            color = cs.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .background(cs.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(provider.model, fontSize = 13.sp, color = cs.onSurfaceVariant)
                Text(
                    provider.endpoint,
                    fontSize = 12.sp,
                    color = cs.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, "编辑", tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.DeleteOutline, "删除", tint = cs.error, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── Edit Dialog ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderEditDialog(
    editingProvider: EditingProviderState,
    isLoading: Boolean,
    error: String?,
    validationResult: ValidationResult?,
    isValidationInProgress: Boolean,
    availableModels: List<String>,
    isFetchingModels: Boolean,
    keyFormatHint: String?,
    endpointFormatHint: String?,
    passwordVisible: Boolean,
    onTogglePassword: () -> Unit,
    onUpdateName: (String) -> Unit,
    onUpdateEndpoint: (String) -> Unit,
    onUpdateApiKey: (String) -> Unit,
    onUpdateModel: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onFetchModels: () -> Unit,
    onApplyPreset: (SettingsViewModel.ProviderPreset) -> Unit,
    onValidateApi: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isEditingExisting: Boolean
) {
    val cs = MaterialTheme.colorScheme
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cs.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Title ──
                Text(
                    if (isEditingExisting) "编辑供应商" else "添加供应商",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = cs.onSurface
                )

                // ── Name ──
                OutlinedTextField(
                    value = editingProvider.name,
                    onValueChange = onUpdateName,
                    label = { Text("供应商名称") },
                    placeholder = { Text("例如：我的 OpenAI") },
                    leadingIcon = { Icon(Icons.Default.Tag, null, tint = cs.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = dialogFieldColors(cs)
                )

                // ── Endpoint ──
                OutlinedTextField(
                    value = editingProvider.endpoint,
                    onValueChange = onUpdateEndpoint,
                    label = { Text("API Endpoint") },
                    placeholder = { Text("https://api.openai.com/v1") },
                    leadingIcon = { Icon(Icons.Default.Link, null, tint = cs.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(12.dp),
                    colors = dialogFieldColors(cs),
                    isError = endpointFormatHint != null && endpointFormatHint?.contains("建议") != true,
                    supportingText = endpointFormatHint?.let {
                        { Text(it, fontSize = 11.sp, color = if (it.contains("建议")) cs.onSurfaceVariant else cs.error) }
                    }
                )

                // ── API Key ──
                OutlinedTextField(
                    value = editingProvider.apiKey,
                    onValueChange = onUpdateApiKey,
                    label = { Text("API Key") },
                    placeholder = { Text("sk-...") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = cs.onSurfaceVariant) },
                    trailingIcon = {
                        IconButton(onClick = onTogglePassword) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null,
                                tint = cs.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    colors = dialogFieldColors(cs),
                    isError = keyFormatHint != null,
                    supportingText = keyFormatHint?.let {
                        { Text(it, fontSize = 11.sp, color = cs.error) }
                    }
                )

                // ── Model ──
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = modelDropdownExpanded && availableModels.isNotEmpty(),
                        onExpandedChange = { modelDropdownExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = editingProvider.model,
                            onValueChange = onUpdateModel,
                            label = { Text("模型") },
                            placeholder = { Text("gpt-3.5-turbo") },
                            trailingIcon = {
                                if (availableModels.isNotEmpty()) {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = dialogFieldColors(cs)
                        )

                        if (availableModels.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = modelDropdownExpanded,
                                onDismissRequest = { modelDropdownExpanded = false }
                            ) {
                                availableModels.forEach { model ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(model, fontSize = 13.sp)
                                                if (model == editingProvider.model) {
                                                    Spacer(Modifier.width(8.dp))
                                                    Icon(Icons.Default.Check, null, tint = cs.primary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        },
                                        onClick = {
                                            onSelectModel(model)
                                            modelDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Fetch models — compact icon button, same height as text field
                    OutlinedButton(
                        onClick = {
                            onFetchModels()
                            modelDropdownExpanded = false
                        },
                        enabled = !isFetchingModels,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        if (isFetchingModels) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = cs.primary)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                                Text("获取", fontSize = 10.sp, color = cs.onSurfaceVariant)
                            }
                        }
                    }
                }

                // ── Presets ──
                Text("快速配置", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = cs.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    SettingsViewModel.ProviderPreset.entries.forEach { preset ->
                        OutlinedButton(
                            onClick = { onApplyPreset(preset) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(preset.label, fontSize = 13.sp)
                        }
                    }
                }

                // ── Validation Result ──
                if (isValidationInProgress) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(cs.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = cs.primary)
                        Spacer(Modifier.width(10.dp))
                        Text("⏳ 正在检测 API 连接...", fontSize = 13.sp, color = cs.onSurfaceVariant)
                    }
                }

                AnimatedVisibility(
                    visible = validationResult != null && !isValidationInProgress,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    validationResult?.let { vr ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(cs.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            ValidationStep("网络连接", vr.reachable)
                            if (vr.reachable) ValidationStep("API Key 认证", vr.authorized)
                            if (vr.authorized) ValidationStep("模型 \"${vr.checkedModel}\"", vr.modelValid)
                            vr.availableModels.takeIf { it.isNotEmpty() }?.let { models ->
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "可用模型: ${models.take(5).joinToString(", ")}${if (models.size > 5) " ..." else ""}",
                                    fontSize = 11.sp,
                                    color = cs.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // ── General error ──
                error?.let {
                    Text(it, color = cs.error, fontSize = 12.sp)
                }

                // ── Action Buttons ──
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("取消")
                    }
                    OutlinedButton(
                        onClick = onValidateApi,
                        enabled = !isLoading && !isValidationInProgress,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🔍 检测", fontSize = 13.sp)
                    }
                    Button(
                        onClick = onSave,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = cs.onPrimary)
                        } else {
                            Text("保存")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationStep(label: String, success: Boolean) {
    val cs = MaterialTheme.colorScheme
    val color = if (success) cs.primary else cs.error
    val icon = if (success) Icons.Default.CheckCircle else Icons.Default.CheckCircle
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            if (success) "✓" else "✗",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 13.sp, color = cs.onSurface)
    }
}

@Composable
private fun dialogFieldColors(cs: androidx.compose.material3.ColorScheme) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = cs.primary,
        unfocusedBorderColor = cs.outline,
        focusedContainerColor = cs.surfaceVariant.copy(alpha = 0.3f),
        unfocusedContainerColor = cs.surfaceVariant.copy(alpha = 0.3f),
        cursorColor = cs.primary,
        focusedLabelColor = cs.primary,
    )
