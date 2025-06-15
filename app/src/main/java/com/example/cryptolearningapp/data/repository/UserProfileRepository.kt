package com.example.cryptolearningapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.cryptolearningapp.data.model.Gender
import com.example.cryptolearningapp.data.model.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    init {
        loadUserProfile()
    }

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

    suspend fun clearUserProfile() {
        prefs.edit().clear().apply()
        _userProfile.value = null
    }

    companion object {
        private const val PREFS_NAME = "user_profile"
        private const val KEY_NAME = "name"
        private const val KEY_GENDER = "gender"
        private const val KEY_BIRTH_YEAR = "birth_year"
    }
} 