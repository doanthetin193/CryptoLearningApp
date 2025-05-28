package com.example.cryptolearningapp.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import com.example.cryptolearningapp.data.model.UserProfile
import com.example.cryptolearningapp.data.repository.CryptoRepository
import com.example.cryptolearningapp.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val cryptoRepository: CryptoRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    suspend fun saveUserProfile(profile: UserProfile) {
        _isLoading.value = true
        try {
            userProfileRepository.saveUserProfile(profile)
            // Reset tiến độ học tập khi lưu thông tin người dùng mới
            cryptoRepository.resetUserProgress("user1") // Giả sử userId là "user1"
        } finally {
            _isLoading.value = false
        }
    }
} 