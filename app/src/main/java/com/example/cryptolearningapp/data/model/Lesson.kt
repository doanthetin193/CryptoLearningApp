package com.example.cryptolearningapp.data.model

data class Lesson(
    val id: Int,
    val title: String,
    val content: String,
    val keywords: List<String>,
    val quiz: List<QuizQuestion>
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val answer: String
)