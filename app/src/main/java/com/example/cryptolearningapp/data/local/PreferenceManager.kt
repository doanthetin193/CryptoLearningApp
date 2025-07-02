package com.example.cryptolearningapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.example.cryptolearningapp.data.model.UserProgress
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    private val _userProgress = MutableStateFlow<UserProgress?>(null)
    val userProgress: StateFlow<UserProgress?> = _userProgress

    init {
        // Load initial data
        _userProgress.value = getUserProgress()
    }

    fun saveUserProgress(progress: UserProgress) {
        sharedPreferences.edit().putString(KEY_USER_PROGRESS, gson.toJson(progress)).apply()
        _userProgress.value = progress
    }

    fun getUserProgress(): UserProgress? {
        val progressJson = sharedPreferences.getString(KEY_USER_PROGRESS, null)
        return progressJson?.let { gson.fromJson(it, UserProgress::class.java) }
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
        _userProgress.value = null
    }

    companion object {
        private const val PREF_NAME = "crypto_learning_pref"
        private const val KEY_USER_PROGRESS = "user_progress"
    }
} 