package com.example.cryptolearningapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cryptolearningapp.ui.viewmodel.ChatUiState
import com.example.cryptolearningapp.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDialog(
    onDismiss: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    var message by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

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
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hỏi Gemini",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng"
                        )
                    }
                }

                // Response area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (uiState) {
                        is ChatUiState.Initial -> {
                            Text(
                                text = "Nhập câu hỏi của bạn ở dưới...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        is ChatUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        is ChatUiState.Success -> {
                            Text(
                                text = (uiState as ChatUiState.Success).message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        is ChatUiState.Error -> {
                            Text(
                                text = (uiState as ChatUiState.Error).message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
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
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (message.isNotBlank()) {
                                viewModel.sendMessage(message)
                                message = ""
                            }
                        },
                        enabled = message.isNotBlank() && uiState !is ChatUiState.Loading
                    ) {
                        Text("Gửi")
                    }
                }
            }
        }
    }
} 