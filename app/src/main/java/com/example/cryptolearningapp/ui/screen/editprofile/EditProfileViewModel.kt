package com.example.cryptolearningapp.ui.screen.editprofile

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
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile

    init {
        viewModelScope.launch {
            userRepository.userProfile.collect { profile ->
                _currentProfile.value = profile
            }
        }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        _isLoading.value = true
        try {
            userRepository.saveUserProfile(profile)
        } finally {
            _isLoading.value = false
        }
    }
} 