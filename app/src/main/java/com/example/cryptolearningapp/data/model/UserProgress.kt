package com.example.cryptolearningapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_progress",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserProgress(
    @PrimaryKey
    val userId: String,
    val completedLessons: List<Int>,
    val totalScore: Int
) 