package com.example.cryptolearningapp.data.model

data class ChatMessage(
    val id: String,
    val message: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
