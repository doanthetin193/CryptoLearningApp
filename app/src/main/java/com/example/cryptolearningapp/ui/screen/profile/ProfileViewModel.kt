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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Giả sử userId là "user1" (có thể lấy từ SharedPreferences hoặc UserProfile nếu cần)
    private val userId = "user1"

    val userDataAndProgress: StateFlow<Pair<UserProfile?, UserProgress?>?> = combine(
        userProfileRepository.userProfile,
        cryptoRepository.getUserProgress(userId)
    ) { profile, progress ->
        if (profile != null) {
            Pair(profile, progress)
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun resetProgress() {
        viewModelScope.launch {
            try {
                // Reset tiến độ trong database
                cryptoRepository.updateUserProgress(userId, emptyList(), 0)
            } catch (e: Exception) {
                _error.value = "Không thể reset tiến độ: ${e.message}"
            }
        }
    }
} 