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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.aichat.app.domain.model.ApiConfig
import com.aichat.app.domain.model.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String? = null,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val cs = MaterialTheme.colorScheme
    var providerMenuExpanded by remember { mutableStateOf(false) }

    // Load conversation if navigating from history
    LaunchedEffect(conversationId) {
        conversationId?.let { viewModel.loadConversation(it) }
    }

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

    val providerName = uiState.apiConfig?.name?.ifBlank { null }
        ?: uiState.apiConfig?.model
        ?: "NexusChat"
    val cacheStats = uiState.cacheStats
    val cacheRateText = if (cacheStats.totalRequests > 0) {
        "\u26a1 ${(cacheStats.hitRate * 100).toInt()}%"
    } else null

    Scaffold(
        containerColor = cs.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.clickable { providerMenuExpanded = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            providerName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        cacheRateText?.let {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                it,
                                fontSize = 11.sp,
                                color = cs.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (uiState.providers.size > 1) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "\u5207\u6362\u4f9b\u5e94\u5546",
                                tint = cs.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = providerMenuExpanded,
                        onDismissRequest = { providerMenuExpanded = false }
                    ) {
                        uiState.providers.forEach { provider ->
                            val isActive = provider.id == uiState.apiConfig?.id
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                provider.name.ifBlank { "\u672a\u547d\u540d" },
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                provider.model,
                                                fontSize = 12.sp,
                                                color = cs.onSurfaceVariant
                                            )
                                        }
                                        if (isActive) {
                                            Spacer(Modifier.width(8.dp))
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = cs.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    providerMenuExpanded = false
                                    if (!isActive) {
                                        viewModel.switchProvider(provider.id)
                                    }
                                }
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::clearMessages) {
                        Icon(Icons.Default.DeleteOutline, "\u6e05\u7a7a\u5bf9\u8bdd", tint = cs.onSurfaceVariant)
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, "\u5386\u53f2\u8bb0\u5f55", tint = cs.onSurfaceVariant)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "\u8bbe\u7f6e", tint = cs.onSurfaceVariant)
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
                            "\u5f00\u59cb\u5bf9\u8bdd",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = cs.onBackground
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "\u8f93\u5165\u6d88\u606f\u6216\u4e0a\u4f20\u56fe\u7247",
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
                    Text("\u56fe\u7247\u5df2\u9009\u62e9", fontSize = 13.sp, color = cs.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = viewModel::clearImage, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, "\u79fb\u9664", tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
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
                            if (showReasoning) "💭 \u601d\u8003\u8fc7\u7a0b ▲" else "💭 \u601d\u8003\u8fc7\u7a0b ▶",
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

        // Token usage & cache indicator
        if (!isUser && message.totalTokens > 0) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.Start
            ) {
                Box(
                    Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(cs.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        buildString {
                            append("\u25cf \u8f93\u5165: ${message.promptTokens} \u25cf \u8f93\u51fa: ${message.completionTokens}")
                            append(" \u25cf \u603b\u8ba1: ${message.totalTokens}")
                            if (message.fromCache) append(" \u26a1 \u7f13\u5b58")
                        },
                        fontSize = 10.sp,
                        color = cs.onSurfaceVariant.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
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
                Icons.Default.AddPhotoAlternate, "\u4e0a\u4f20\u56fe\u7247",
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
                        Text("\u8f93\u5165\u6d88\u606f...", color = cs.onSurfaceVariant, fontSize = 15.sp)
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
                        Icons.AutoMirrored.Filled.Send, "\u53d1\u9001",
                        tint = if (canSend) cs.onPrimary else cs.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
