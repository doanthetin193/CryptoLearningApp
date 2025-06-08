package com.example.cryptolearningapp.ui.screen.lesson

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cryptolearningapp.ui.components.ChatDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    lessonId: Int,
    onBackClick: () -> Unit,
    onQuizClick: () -> Unit,
    viewModel: LessonViewModel = hiltViewModel()
) {
    val lesson by viewModel.lesson.collectAsState()
    var selectedKeyword by remember { mutableStateOf<String?>(null) }
    var showChatDialog by remember { mutableStateOf(false) }

    LaunchedEffect(lessonId) {
        viewModel.loadLesson(lessonId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bài học") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onQuizClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Làm Quiz")
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showChatDialog = true }
            ) {
                Icon(Icons.Default.Chat, contentDescription = "Hỏi Gemini")
            }
        }
    ) { paddingValues ->
        lesson?.let { currentLesson ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = buildAnnotatedString {
                        val words = currentLesson.content.split(" ")
                        words.forEach { word ->
                            if (currentLesson.keywords.contains(word)) {
                                withStyle(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append(word)
                                }
                            } else {
                                append(word)
                            }
                            append(" ")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    // Keyword Definition Dialog
    selectedKeyword?.let { keyword ->
        AlertDialog(
            onDismissRequest = { selectedKeyword = null },
            title = { Text(keyword) },
            text = {
                // Here we would show the AI-generated definition
                Text("Loading definition...")
            },
            confirmButton = {
                TextButton(onClick = { selectedKeyword = null }) {
                    Text("Đóng")
                }
            }
        )
    }

    if (showChatDialog) {
        ChatDialog(
            onDismiss = { showChatDialog = false }
        )
    }
} 