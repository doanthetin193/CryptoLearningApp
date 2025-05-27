package com.example.cryptolearningapp.data.model

data class Lesson(
    val id: Int,
    val title: String,
    val content: String,
    val keywords: List<String>,
    val quiz: Quiz
)

data class Quiz(
    val question: String,
    val options: List<String>,
    val answer: String
)

data class GlossaryTerm(
    val term: String,
    val definition: String
) 