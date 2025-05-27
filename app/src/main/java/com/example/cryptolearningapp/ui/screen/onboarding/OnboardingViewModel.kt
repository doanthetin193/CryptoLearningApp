package com.example.cryptolearningapp.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import com.example.cryptolearningapp.data.model.UserProfile
import com.example.cryptolearningapp.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    suspend fun saveUserProfile(profile: UserProfile) {
        _isLoading.value = true
        try {
            userProfileRepository.saveUserProfile(profile)
        } finally {
            _isLoading.value = false
        }
    }
} 