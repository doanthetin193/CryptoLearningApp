package com.example.cryptolearningapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.cryptolearningapp.data.model.Gender
import com.example.cryptolearningapp.data.model.UserProfile
import com.example.cryptolearningapp.data.model.UserProgress
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // User Profile
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    // User Progress
    private val _userProgress = MutableStateFlow<UserProgress?>(null)
    val userProgress: StateFlow<UserProgress?> = _userProgress

    init {
        loadUserData()
    }

    private fun loadUserData() {
        loadUserProfile()
        loadUserProgress()
    }

    // Profile Methods
    private fun loadUserProfile() {
        val name = prefs.getString(KEY_NAME, null)
        val genderStr = prefs.getString(KEY_GENDER, null)
        val birthYear = prefs.getInt(KEY_BIRTH_YEAR, -1)

        if (name != null && genderStr != null && birthYear != -1) {
            _userProfile.value = UserProfile(
                name = name,
                gender = Gender.valueOf(genderStr),
                birthYear = birthYear
            )
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        prefs.edit().apply {
            putString(KEY_NAME, profile.name)
            putString(KEY_GENDER, profile.gender.name)
            putInt(KEY_BIRTH_YEAR, profile.birthYear)
            apply()
        }
        _userProfile.value = profile
    }

    // Progress Methods
    private fun loadUserProgress() {
        val progressJson = prefs.getString(KEY_USER_PROGRESS, null)
        _userProgress.value = progressJson?.let { gson.fromJson(it, UserProgress::class.java) }
    }

    suspend fun saveUserProgress(progress: UserProgress) {
        prefs.edit().putString(KEY_USER_PROGRESS, gson.toJson(progress)).apply()
        _userProgress.value = progress
    }

    suspend fun updateProgress(completedLessons: List<Int>, totalScore: Int) {
        val currentProgress = _userProgress.value
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
        saveUserProgress(newProgress)
    }

    suspend fun resetProgress() {
        val newProgress = UserProgress(
            completedLessons = emptyList(),
            totalScore = 0
        )
        saveUserProgress(newProgress)
    }

    suspend fun clearAllData() {
        prefs.edit().clear().apply()
        _userProfile.value = null
        _userProgress.value = null
    }

    companion object {
        private const val PREFS_NAME = "crypto_learning_app"
        private const val KEY_NAME = "name"
        private const val KEY_GENDER = "gender"
        private const val KEY_BIRTH_YEAR = "birth_year"
        private const val KEY_USER_PROGRESS = "user_progress"
    }
}
