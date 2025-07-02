package com.example.cryptolearningapp.data.model

data class ChatSession(
    val id: String,
    val lessonId: Int? = null,
    val lessonTitle: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val messages: List<ChatMessage> = emptyList()
)

data class ChatSessionHistory(
    val sessions: List<ChatSession> = emptyList(),
    val currentSessionId: String? = null
)
