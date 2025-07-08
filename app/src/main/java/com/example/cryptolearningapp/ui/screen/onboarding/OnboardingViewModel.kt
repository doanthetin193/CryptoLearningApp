package com.example.cryptolearningapp.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptolearningapp.data.model.UserProfile
import com.example.cryptolearningapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.saveUserProfile(profile)
                // Reset tiến độ học tập khi lưu thông tin người dùng mới
                userRepository.resetProgress()
            } finally {
                _isLoading.value = false
            }
        }
    }
} 