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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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

    AlertDialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Text(
                if (isEditingExisting) "编辑供应商" else "添加供应商",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── Name ──
                OutlinedTextField(
                    value = editingProvider.name,
                    onValueChange = onUpdateName,
                    label = { Text("供应商名称") },
                    placeholder = { Text("例如：我的 OpenAI") },
                    leadingIcon = { Icon(Icons.Default.Tag, null, tint = cs.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogFieldColors(cs)
                )

                // ── Endpoint ──
                OutlinedTextField(
                    value = editingProvider.endpoint,
                    onValueChange = onUpdateEndpoint,
                    label = { Text("API Endpoint") },
                    placeholder = { Text("https://api.openai.com/v1") },
                    leadingIcon = { Icon(Icons.Default.Link, null, tint = cs.onSurfaceVariant) },
                    trailingIcon = {
                        if (editingProvider.endpoint.isNotBlank()) {
                            when {
                                endpointFormatHint != null -> Icon(
                                    Icons.Default.WarningAmber,
                                    contentDescription = endpointFormatHint,
                                    tint = if (endpointFormatHint!!.contains("建议")) cs.onSurfaceVariant else cs.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                                else -> Icon(
                                    Icons.Default.Check,
                                    contentDescription = "格式正确",
                                    tint = cs.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogFieldColors(cs),
                    supportingText = endpointFormatHint?.let {
                        { Text(it, fontSize = 12.sp, color = if (it.contains("建议")) cs.onSurfaceVariant else cs.error.copy(alpha = 0.7f)) }
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Key format indicator
                            if (editingProvider.apiKey.isNotBlank()) {
                                when {
                                    keyFormatHint != null -> Icon(
                                        Icons.Default.WarningAmber,
                                        contentDescription = keyFormatHint,
                                        tint = cs.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    else -> Icon(
                                        Icons.Default.Check,
                                        contentDescription = "格式正确",
                                        tint = cs.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                            }
                            IconButton(onClick = onTogglePassword) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null,
                                    tint = cs.onSurfaceVariant
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogFieldColors(cs),
                    supportingText = keyFormatHint?.let {
                        { Text(it, fontSize = 12.sp, color = cs.error.copy(alpha = 0.7f)) }
                    }
                )

                // ── Model (dropdown + fetch) ──
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                            shape = RoundedCornerShape(10.dp),
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
                                                    Icon(
                                                        Icons.Default.Check, null,
                                                        tint = cs.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
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

                    // Fetch models button
                    OutlinedButton(
                        onClick = {
                            onFetchModels()
                            modelDropdownExpanded = false
                        },
                        enabled = !isFetchingModels,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        if (isFetchingModels) {
                            CircularProgressIndicator(
                                Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = cs.primary
                            )
                        } else {
                            Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                        }
                    }
                }

                // ── Presets ──
                Text("快速配置", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = cs.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                    SettingsViewModel.ProviderPreset.entries.forEach { preset ->
                        OutlinedButton(
                            onClick = { onApplyPreset(preset) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(preset.label, fontSize = 12.sp)
                        }
                    }
                }

                // ── Validation Result ──
                if (isValidationInProgress) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(cs.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = cs.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("⏳ 正在检测...", fontSize = 13.sp, color = cs.onSurfaceVariant)
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
                                .background(cs.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ValidationStep(
                                label = if (vr.reachable) "✓ 网络可达" else "✗ 网络不可达",
                                success = vr.reachable
                            )
                            if (vr.reachable) {
                                ValidationStep(
                                    label = if (vr.authorized) "✓ API Key 有效" else "✗ API Key 无效",
                                    success = vr.authorized
                                )
                            }
                            if (vr.authorized) {
                                ValidationStep(
                                    label = if (vr.modelValid) "✓ 模型可用 (${vr.checkedModel})"
                                    else "⚠ 模型 \"${vr.checkedModel}\" 不在可用列表中",
                                    success = vr.modelValid,
                                    isWarning = !vr.modelValid
                                )
                            }
                            vr.availableModels.takeIf { it.isNotEmpty() }?.let { models ->
                                Text(
                                    "可用模型: ${models.take(5).joinToString(", ")}",
                                    fontSize = 11.sp,
                                    color = cs.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 20.dp)
                                )
                            }
                            vr.stepErrors.takeIf { it.isNotEmpty() }?.let { errors ->
                                errors.forEach { err ->
                                    Text(err, fontSize = 11.sp, color = cs.error)
                                }
                            }
                        }
                    }
                }

                // ── General error ──
                error?.let {
                    Text(it, color = cs.error, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = cs.onPrimary)
                } else {
                    Text("保存")
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = onValidateApi,
                    enabled = !isLoading && !isValidationInProgress,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isValidationInProgress) {
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(4.dp))
                    }
                    Text("🔍 全面检测", fontSize = 13.sp)
                }
                TextButton(onClick = onCancel) {
                    Text("取消")
                }
            }
        },
        containerColor = cs.surface,
        tonalElevation = 0.dp
    )
}

@Composable
private fun ValidationStep(
    label: String,
    success: Boolean,
    isWarning: Boolean = false
) {
    val cs = MaterialTheme.colorScheme
    val color = when {
        isWarning -> cs.error.copy(alpha = 0.8f)
        success -> cs.primary
        else -> cs.error
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (isWarning) Icons.Default.WarningAmber else if (success) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp, color = color)
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
