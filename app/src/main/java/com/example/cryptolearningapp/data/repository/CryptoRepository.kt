package com.example.cryptolearningapp.data.repository

import com.example.cryptolearningapp.data.local.PreferenceManager
import com.example.cryptolearningapp.data.model.User
import com.example.cryptolearningapp.data.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoRepository @Inject constructor(
    private val preferenceManager: PreferenceManager
) {
    fun getUser(userId: String): Flow<User?> = flow {
        emit(preferenceManager.getUser())
    }

    suspend fun saveUser(user: User) {
        preferenceManager.saveUser(user)
        // Initialize progress for new user
        preferenceManager.saveUserProgress(
            UserProgress(
                userId = user.id,
                completedLessons = emptyList(),
                totalScore = 0
            )
        )
    }

    fun getUserProgress(userId: String): StateFlow<UserProgress?> {
        return preferenceManager.userProgress
    }

    suspend fun updateProgress(userId: String, completedLessons: List<Int>, totalScore: Int) {
        val currentProgress = preferenceManager.getUserProgress()
        val newProgress = if (currentProgress != null) {
            currentProgress.copy(
                completedLessons = completedLessons,
                totalScore = totalScore
            )
        } else {
            UserProgress(
                userId = userId,
                completedLessons = completedLessons,
                totalScore = totalScore
            )
        }
        preferenceManager.saveUserProgress(newProgress)
    }

    suspend fun resetProgress(userId: String) {
        val currentProgress = preferenceManager.getUserProgress()
        val newProgress = if (currentProgress != null) {
            currentProgress.copy(
                completedLessons = emptyList(),
                totalScore = 0
            )
        } else {
            UserProgress(
                userId = userId,
                completedLessons = emptyList(),
                totalScore = 0
            )
        }
        preferenceManager.saveUserProgress(newProgress)
    }
} 