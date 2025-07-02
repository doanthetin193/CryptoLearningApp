package com.example.cryptolearningapp.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptolearningapp.data.model.UserProfile
import com.example.cryptolearningapp.data.model.UserProgress
import com.example.cryptolearningapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val userProfile: StateFlow<UserProfile?> = userRepository.userProfile
    val userProgress: StateFlow<UserProgress?> = userRepository.userProgress

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Data is automatically loaded by UserRepository
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun updateProgress(completedLessons: List<Int>, totalScore: Int) {
        viewModelScope.launch {
            try {
                userRepository.updateProgress(completedLessons, totalScore)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            try {
                userRepository.resetProgress()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
} 