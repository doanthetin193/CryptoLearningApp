package com.example.cryptolearningapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.cryptolearningapp.data.model.UserProgress
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProgressRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _userProgress = MutableStateFlow<UserProgress?>(null)
    val userProgress: Flow<UserProgress?> = _userProgress.asStateFlow()

    init {
        loadUserProgress()
    }

    private fun loadUserProgress() {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return
        val completedLessons = prefs.getStringSet(KEY_COMPLETED_LESSONS, emptySet())?.map { it.toInt() } ?: emptyList()
        val totalScore = prefs.getInt(KEY_TOTAL_SCORE, 0)

        _userProgress.value = UserProgress(
            userId = userId,
            completedLessons = completedLessons,
            totalScore = totalScore
        )
    }

    suspend fun setUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
        loadUserProgress()
    }

    suspend fun resetProgress() {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return
        prefs.edit().apply {
            putStringSet(KEY_COMPLETED_LESSONS, emptySet())
            putInt(KEY_TOTAL_SCORE, 0)
            apply()
        }
        _userProgress.value = UserProgress(
            userId = userId,
            completedLessons = emptyList(),
            totalScore = 0
        )
    }

    suspend fun updateProgress(completedLessons: List<Int>, totalScore: Int) {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return
        prefs.edit().apply {
            putStringSet(KEY_COMPLETED_LESSONS, completedLessons.map { it.toString() }.toSet())
            putInt(KEY_TOTAL_SCORE, totalScore)
            apply()
        }
        _userProgress.value = UserProgress(
            userId = userId,
            completedLessons = completedLessons,
            totalScore = totalScore
        )
    }

    companion object {
        private const val PREFS_NAME = "user_progress"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_COMPLETED_LESSONS = "completed_lessons"
        private const val KEY_TOTAL_SCORE = "total_score"
    }
} 