package com.aichat.app.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.aichat.app.domain.model.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val cs = MaterialTheme.colorScheme

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setSelectedImage(it.toString()) }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.dismissError()
        }
    }

    Scaffold(
        containerColor = cs.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("NexusChat", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                },
                actions = {
                    IconButton(onClick = viewModel::clearMessages) {
                        Icon(Icons.Default.DeleteOutline, "清空对话", tint = cs.onSurfaceVariant)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "设置", tint = cs.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.surface,
                    titleContentColor = cs.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            if (uiState.messages.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💬", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "开始对话",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = cs.onBackground
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "输入消息或上传图片",
                            fontSize = 14.sp,
                            color = cs.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.messages) { message ->
                        MessageBubble(message)
                    }
                    if (uiState.isLoading) {
                        item {
                            Row(Modifier.fillMaxWidth(), Arrangement.Start) {
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(cs.surfaceVariant)
                                        .padding(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = cs.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // image preview
            uiState.selectedImageUri?.let { uri ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(cs.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        rememberAsyncImagePainter(uri),
                        null,
                        Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("图片已选择", fontSize = 13.sp, color = cs.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = viewModel::clearImage, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, "移除", tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }

            ChatInputBar(
                text = uiState.inputText,
                onTextChange = viewModel::updateInputText,
                onSend = viewModel::sendMessage,
                onPickImage = { imagePicker.launch("image/*") },
                enabled = !uiState.isLoading,
                isLoading = uiState.isLoading
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val cs = MaterialTheme.colorScheme
    val isUser = message.isUser
    var showReasoning by remember { mutableStateOf(false) }

    val bubbleColor = if (isUser) cs.primary else cs.surfaceVariant
    val textColor   = if (isUser) cs.onPrimary else cs.onSurface
    val shape = RoundedCornerShape(
        topStart = 16.dp, topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd   = if (isUser) 4.dp else 16.dp
    )

    Column(
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        message.imageUri?.let { uri ->
            Image(
                rememberAsyncImagePainter(uri),
                null,
                Modifier
                    .widthIn(max = 220.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.FillWidth
            )
            Spacer(Modifier.height(4.dp))
        }

        if (!isUser && !message.reasoningContent.isNullOrBlank()) {
            Row(Modifier.fillMaxWidth(), Arrangement.Start) {
                Box(
                    Modifier
                        .widthIn(max = 280.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cs.surfaceVariant.copy(alpha = 0.7f))
                        .clickable { showReasoning = !showReasoning }
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            if (showReasoning) "💭 思考过程 ▲" else "💭 思考过程 ▶",
                            fontSize = 12.sp,
                            color = cs.onSurfaceVariant
                        )
                        AnimatedVisibility(showReasoning) {
                            Text(
                                message.reasoningContent,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = cs.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        if (message.content.isNotBlank()) {
            Row(
                Modifier.fillMaxWidth(),
                if (isUser) Arrangement.End else Arrangement.Start
            ) {
                Box(
                    Modifier
                        .widthIn(max = 280.dp)
                        .clip(shape)
                        .background(bubbleColor)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        message.content,
                        color = textColor,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onPickImage: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean
) {
    val cs = MaterialTheme.colorScheme
    val canSend = enabled && (text.isNotBlank() || isLoading)

    Row(
        Modifier
            .fillMaxWidth()
            .background(cs.surface)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPickImage, enabled = enabled, modifier = Modifier.size(40.dp)) {
            Icon(
                Icons.Default.AddPhotoAlternate, "上传图片",
                tint = if (enabled) cs.onSurfaceVariant else cs.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(22.dp)
            )
        }

        Box(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(cs.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = text,
                onValueChange = onTextChange,
                enabled = enabled,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = cs.onSurface),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (text.isEmpty()) {
                        Text("输入消息...", color = cs.onSurfaceVariant, fontSize = 15.sp)
                    }
                    inner()
                }
            )
        }

        Spacer(Modifier.width(6.dp))

        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (canSend) cs.primary else cs.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = cs.onPrimary)
            } else {
                IconButton(
                    onClick = onSend,
                    enabled = canSend,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send, "发送",
                        tint = if (canSend) cs.onPrimary else cs.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
