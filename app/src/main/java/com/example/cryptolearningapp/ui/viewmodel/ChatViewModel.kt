package com.example.cryptolearningapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptolearningapp.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initial)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            repository.sendMessage(message)
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