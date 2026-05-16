package com.aichat.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    onNavigateToChat: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateToChat()
    }

    Scaffold(containerColor = cs.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            Text("⚙️ 设置", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = cs.onBackground)
            Spacer(Modifier.height(4.dp))
            Text("配置 API 连接", fontSize = 14.sp, color = cs.onSurfaceVariant)

            Spacer(Modifier.height(28.dp))

            // ── Endpoint ─────────────────────────────────
            OutlinedTextField(
                value = uiState.endpoint,
                onValueChange = viewModel::updateEndpoint,
                label = { Text("API Endpoint") },
                placeholder = { Text("https://api.openai.com/v1") },
                leadingIcon = { Icon(Icons.Default.Link, null, tint = cs.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(cs)
            )

            Spacer(Modifier.height(12.dp))

            // ── API Key ──────────────────────────────────
            OutlinedTextField(
                value = uiState.apiKey,
                onValueChange = viewModel::updateApiKey,
                label = { Text("API Key") },
                placeholder = { Text("sk-...") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = cs.onSurfaceVariant) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
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
                colors = fieldColors(cs)
            )

            Spacer(Modifier.height(12.dp))

            // ── Model ────────────────────────────────────
            OutlinedTextField(
                value = uiState.model,
                onValueChange = viewModel::updateModel,
                label = { Text("模型") },
                placeholder = { Text("gpt-3.5-turbo") },
                leadingIcon = { Icon(Icons.Default.ModelTraining, null, tint = cs.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(cs)
            )

            Spacer(Modifier.height(16.dp))

            // ── Presets ──────────────────────────────────
            Text("快速配置", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = cs.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                SettingsViewModel.ProviderPreset.entries.forEach { preset ->
                    OutlinedButton(
                        onClick = { viewModel.applyPreset(preset) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.primary)
                    ) {
                        Text(preset.label, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Status ───────────────────────────────────
            uiState.error?.let {
                Text(it, color = cs.error, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
            uiState.testSuccess?.let { ok ->
                Text(
                    if (ok) "✓ 连接成功" else "✗ 连接失败",
                    color = if (ok) cs.primary else cs.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // ── Buttons ──────────────────────────────────
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = viewModel::testConnection,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = cs.primary)
                    } else {
                        Text("测试连接")
                    }
                }
                Button(
                    onClick = viewModel::saveConfig,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                ) {
                    Text("保存并开始")
                }
            }
        }
    }
}

@Composable
private fun fieldColors(cs: androidx.compose.material3.ColorScheme) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = cs.primary,
        unfocusedBorderColor = cs.outline,
        focusedContainerColor = cs.surface,
        unfocusedContainerColor = cs.surface,
        cursorColor = cs.primary,
        focusedLabelColor = cs.primary,
    )
