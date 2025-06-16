package com.example.cryptolearningapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptolearningapp.data.repository.ChatRepository
import com.example.cryptolearningapp.data.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val cryptoRepository: CryptoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initial)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _canUseChat = MutableStateFlow(false)
    val canUseChat: StateFlow<Boolean> = _canUseChat.asStateFlow()

    init {
        checkUserScore()
    }

    private fun checkUserScore() {
        viewModelScope.launch {
            cryptoRepository.getUserProgress("user1")
                .collect { progress ->
                    _canUseChat.value = progress?.totalScore ?: 0 >= 50
                }
        }
    }

    fun sendMessage(message: String) {
        if (!_canUseChat.value) {
            _uiState.value = ChatUiState.Error("Bạn cần đạt ít nhất 50 điểm để sử dụng tính năng này")
            return
        }

        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            chatRepository.sendMessage(message)
                .onSuccess { response ->
                    _uiState.value = ChatUiState.Success(response)
                }
                .onFailure { error ->
                    _uiState.value = ChatUiState.Error(error.message ?: "Có lỗi xảy ra")
                }
        }
    }
}

sealed class ChatUiState {
    object Initial : ChatUiState()
    object Loading : ChatUiState()
    data class Success(val message: String) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
} 