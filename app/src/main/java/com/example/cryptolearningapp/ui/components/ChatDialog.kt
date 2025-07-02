package com.example.cryptolearningapp.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cryptolearningapp.data.model.ChatMessage
import com.example.cryptolearningapp.data.model.ChatSession
import com.example.cryptolearningapp.ui.viewmodel.ChatUiState
import com.example.cryptolearningapp.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDialog(
    onDismiss: () -> Unit,
    lessonId: Int = 0,
    lessonTitle: String = "Chat chung",
    viewModel: ChatViewModel = hiltViewModel()
) {
    var message by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val canUseChat by viewModel.canUseChat.collectAsState()
    val currentSession by viewModel.currentChatSession.collectAsState()
    val sessionHistory by viewModel.chatSessionHistory.collectAsState()
    val showingHistory by viewModel.showingSessionHistory.collectAsState()
    val listState = rememberLazyListState()
    
    // Tự động tạo session mới mỗi khi mở chat dialog
    LaunchedEffect(Unit) {
        // Luôn tạo session mới khi mở dialog
        viewModel.startNewSession(lessonId, lessonTitle)
    }

    // Auto scroll to bottom when new message added
    LaunchedEffect(currentSession?.messages?.size) {
        currentSession?.let { session ->
            if (session.messages.isNotEmpty()) {
                listState.animateScrollToItem(session.messages.size - 1)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with dynamic title based on view state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button if showing history
                        if (showingHistory) {
                            IconButton(onClick = { viewModel.toggleSessionHistory() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Quay lại"
                                )
                            }
                        }
                        
                        Text(
                            text = if (showingHistory) {
                                "Lịch sử chat"
                            } else {
                                currentSession?.lessonTitle ?: "Chat mới"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Row {
                        // History button
                        IconButton(onClick = { viewModel.toggleSessionHistory() }) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Lịch sử chat",
                                tint = if (showingHistory) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                        
                        // New chat button (when viewing a session)
                        if (!showingHistory) {
                            IconButton(onClick = { viewModel.startNewSession(lessonId, lessonTitle) }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Chat mới"
                                )
                            }
                        }
                        
                        // Clear button (when viewing a session with messages)
                        if (!showingHistory && (currentSession?.messages?.isNotEmpty() == true)) {
                            IconButton(onClick = { viewModel.clearCurrentSession() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Xóa chat",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        
                        // Close dialog button
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Đóng"
                            )
                        }
                    }
                }

                // Main content area - either chat or session history
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (showingHistory) {
                        // Session history view
                        SessionHistoryList(
                            sessions = sessionHistory.sessions,
                            onSelectSession = { sessionId -> viewModel.loadSession(sessionId) },
                            onDeleteSession = { sessionId -> viewModel.deleteSession(sessionId) },
                            onClearAllSessions = { viewModel.clearAllSessions() }
                        )
                    } else {
                        // Current chat view
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!canUseChat) {
                                item {
                                    Text(
                                        text = "Bạn cần đạt ít nhất 50 điểm để sử dụng tính năng này.\nHãy hoàn thành các bài học và quiz để tích lũy điểm!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    )
                                }
                            } else if (currentSession != null) {
                                currentSession?.let { session ->
                                    items(session.messages) { chatMessage ->
                                        ChatMessageBubble(chatMessage)
                                    }
                                }
                                
                                // Show current state
                                when (uiState) {
                                    is ChatUiState.Loading -> {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator()
                                            }
                                        }
                                    }
                                    is ChatUiState.Error -> {
                                        item {
                                            val errorState = uiState as ChatUiState.Error
                                            Text(
                                                text = errorState.message,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp)
                                            )
                                        }
                                    }
                                    is ChatUiState.Initial -> {
                                        currentSession?.let { session ->
                                            if (session.messages.isEmpty()) {
                                                item {
                                                    Text(
                                                    text = "Nhập câu hỏi của bạn ở dưới...",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp)
                                                )
                                                }
                                            }
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }

                // Input area
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nhập câu hỏi của bạn...") },
                        singleLine = true,
                        enabled = canUseChat
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (message.isNotBlank()) {
                                viewModel.sendMessage(message)
                                message = ""
                            }
                        },
                        enabled = message.isNotBlank() && uiState !is ChatUiState.Loading && canUseChat
                    ) {
                        Text("Gửi")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(chatMessage: ChatMessage) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (chatMessage.isFromUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (chatMessage.isFromUser) 16.dp else 4.dp,
                            bottomEnd = if (chatMessage.isFromUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (chatMessage.isFromUser) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = chatMessage.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (chatMessage.isFromUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Text(
                text = timeFormat.format(Date(chatMessage.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(
                    start = if (chatMessage.isFromUser) 0.dp else 8.dp,
                    end = if (chatMessage.isFromUser) 8.dp else 0.dp,
                    top = 4.dp
                )
            )
        }
    }
} 