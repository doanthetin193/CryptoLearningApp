package com.example.cryptolearningapp.data.model

data class UserProgress(
    val userId: String,
    val completedLessons: List<Int>,
    val totalScore: Int
) 