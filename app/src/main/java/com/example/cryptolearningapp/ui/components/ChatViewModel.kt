package com.example.cryptolearningapp.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptolearningapp.data.repository.ChatRepository
import com.example.cryptolearningapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initial)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _canUseChat = MutableStateFlow(false)
    val canUseChat: StateFlow<Boolean> = _canUseChat.asStateFlow()

    // Current chat session từ UserRepository
    val currentChatSession = userRepository.currentChatSession
    
    // Session history
    val chatSessionHistory = userRepository.chatSessionHistory
    
    // Trạng thái hiển thị lịch sử sessions
    private val _showingSessionHistory = MutableStateFlow(false)
    val showingSessionHistory: StateFlow<Boolean> = _showingSessionHistory

    init {
        checkUserScore()
    }

    private fun checkUserScore() {
        viewModelScope.launch {
            userRepository.userProgress
                .collect { progress ->
                    _canUseChat.value = progress?.totalScore ?: 0 >= 50
                }
        }
    }

    /**
     * Khởi tạo một chat session mới
     */
    fun startNewSession(lessonId: Int, lessonTitle: String) {
        viewModelScope.launch {
            userRepository.startNewChatSession(lessonId, lessonTitle)
            _showingSessionHistory.value = false
            _uiState.value = ChatUiState.Initial
        }
    }
    
    /**
     * Gửi tin nhắn trong session hiện tại
     */
    fun sendMessage(message: String) {
        if (!_canUseChat.value) {
            _uiState.value =
                ChatUiState.Error("Bạn cần đạt ít nhất 50 điểm để sử dụng tính năng này")
            return
        }
        
        // Đảm bảo có một session đang active
        if (currentChatSession.value == null) {
            _uiState.value = ChatUiState.Error("Không có session chat đang hoạt động")
            return
        }

        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            
            // Lưu tin nhắn của user vào session
            userRepository.addMessageToCurrentSession(message, isFromUser = true)
            
            chatRepository.sendMessage(message)
                .onSuccess { response ->
                    // Lưu phản hồi của AI vào session
                    userRepository.addMessageToCurrentSession(response, isFromUser = false)
                    _uiState.value = ChatUiState.Success(response)
                }
                .onFailure { error ->
                    _uiState.value = ChatUiState.Error(error.message ?: "Có lỗi xảy ra")
                }
        }
    }

    /**
     * Xóa session chat hiện tại
     */
    fun clearCurrentSession() {
        viewModelScope.launch {
            userRepository.clearCurrentChatSession()
            _uiState.value = ChatUiState.Initial
        }
    }
    
    /**
     * Xóa tất cả các sessions
     */
    fun clearAllSessions() {
        viewModelScope.launch {
            userRepository.clearAllChatSessions()
            _uiState.value = ChatUiState.Initial
            _showingSessionHistory.value = false
        }
    }
    
    /**
     * Mở session cũ
     */
    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            userRepository.loadChatSession(sessionId)
            _showingSessionHistory.value = false
        }
    }
    
    /**
     * Hiện/ẩn lịch sử session
     */
    fun toggleSessionHistory() {
        _showingSessionHistory.value = !_showingSessionHistory.value
    }
    
    /**
     * Xóa một session cụ thể
     */
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            userRepository.deleteChatSession(sessionId)
            // Nếu đang ở trang history, giữ nguyên trang đó
            // Nếu không, đặt trạng thái về Initial
            if (!_showingSessionHistory.value) {
                _uiState.value = ChatUiState.Initial
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