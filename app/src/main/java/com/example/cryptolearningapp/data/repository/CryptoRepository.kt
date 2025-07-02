package com.example.cryptolearningapp.data.repository

import com.example.cryptolearningapp.data.local.PreferenceManager
import com.example.cryptolearningapp.data.model.UserProgress
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoRepository @Inject constructor(
    private val preferenceManager: PreferenceManager
) {
    fun getUserProgress(): StateFlow<UserProgress?> {
        return preferenceManager.userProgress
    }

    suspend fun updateProgress(completedLessons: List<Int>, totalScore: Int) {
        val currentProgress = preferenceManager.getUserProgress()
        val newProgress = if (currentProgress != null) {
            currentProgress.copy(
                completedLessons = completedLessons,
                totalScore = totalScore
            )
        } else {
            UserProgress(
                completedLessons = completedLessons,
                totalScore = totalScore
            )
        }
        preferenceManager.saveUserProgress(newProgress)
    }

    suspend fun resetProgress() {
        val newProgress = UserProgress(
            completedLessons = emptyList(),
            totalScore = 0
        )
        preferenceManager.saveUserProgress(newProgress)
    }
} 