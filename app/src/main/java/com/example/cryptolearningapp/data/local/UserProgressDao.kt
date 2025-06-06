package com.example.cryptolearningapp.data.dao

import androidx.room.*
import com.example.cryptolearningapp.data.model.UserProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    fun getUserProgress(userId: String): Flow<UserProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(userProgress: UserProgress)

    @Query("UPDATE user_progress SET completedLessons = :completedLessons, totalScore = :totalScore WHERE userId = :userId")
    suspend fun updateProgress(userId: String, completedLessons: List<Int>, totalScore: Int)
} 