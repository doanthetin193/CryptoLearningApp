package com.example.cryptolearningapp.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptolearningapp.data.model.UserProfile
import com.example.cryptolearningapp.data.model.UserProgress
import com.example.cryptolearningapp.data.repository.CryptoRepository
import com.example.cryptolearningapp.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val cryptoRepository: CryptoRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val userId = "user1"

    val userProfile: StateFlow<UserProfile?> = userProfileRepository.userProfile
    val userProgress: StateFlow<UserProgress?> = cryptoRepository.getUserProgress(userId)

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load user profile and progress
                combine(
                    userProfile,
                    userProgress
    ) { profile, progress ->
                    // Both flows will emit their values
                    _isLoading.value = false
                }.collect()
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
        }
        }
    }

    fun updateProgress(completedLessons: List<Int>, totalScore: Int) {
        viewModelScope.launch {
            try {
                cryptoRepository.updateProgress(userId, completedLessons, totalScore)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            try {
                cryptoRepository.resetProgress(userId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
} 