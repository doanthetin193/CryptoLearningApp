package com.example.cryptolearningapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.cryptolearningapp.data.model.ChatMessage
import com.example.cryptolearningapp.data.model.ChatSession
import com.example.cryptolearningapp.data.model.ChatSessionHistory
import com.example.cryptolearningapp.data.model.Gender
import com.example.cryptolearningapp.data.model.UserProfile
import com.example.cryptolearningapp.data.model.UserProgress
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // User Profile
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    // User Progress
    private val _userProgress = MutableStateFlow<UserProgress?>(null)
    val userProgress: StateFlow<UserProgress?> = _userProgress

    // Chat Sessions
    private val _chatSessionHistory = MutableStateFlow<ChatSessionHistory>(ChatSessionHistory())
    val chatSessionHistory: StateFlow<ChatSessionHistory> = _chatSessionHistory
    
    private val _currentChatSession = MutableStateFlow<ChatSession?>(null)
    val currentChatSession: StateFlow<ChatSession?> = _currentChatSession

    init {
        loadUserData()
    }

    private fun loadUserData() {
        loadUserProfile()
        loadUserProgress()
        loadChatSessionHistory()
    }

    // Profile Methods
    private fun loadUserProfile() {
        val name = prefs.getString(KEY_NAME, null)
        val genderStr = prefs.getString(KEY_GENDER, null)
        val birthYear = prefs.getInt(KEY_BIRTH_YEAR, -1)

        if (name != null && genderStr != null && birthYear != -1) {
            _userProfile.value = UserProfile(
                name = name,
                gender = Gender.valueOf(genderStr),
                birthYear = birthYear
            )
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        prefs.edit().apply {
            putString(KEY_NAME, profile.name)
            putString(KEY_GENDER, profile.gender.name)
            putInt(KEY_BIRTH_YEAR, profile.birthYear)
            apply()
        }
        _userProfile.value = profile
    }

    // Progress Methods
    private fun loadUserProgress() {
        val progressJson = prefs.getString(KEY_USER_PROGRESS, null)
        _userProgress.value = progressJson?.let { gson.fromJson(it, UserProgress::class.java) }
    }

    suspend fun saveUserProgress(progress: UserProgress) {
        prefs.edit().putString(KEY_USER_PROGRESS, gson.toJson(progress)).apply()
        _userProgress.value = progress
    }

    suspend fun updateProgress(completedLessons: List<Int>, totalScore: Int) {
        val currentProgress = _userProgress.value
        val newProgress = if (currentProgress != null) {
            currentProgress.copy(
                completedLessons = completedLessons,
                totalScore = totalScore
            )
        } else {
            UserProgress(
                completedLessons = completedLessons,
                totalScore = totalScore
            )
        }
        saveUserProgress(newProgress)
    }

    suspend fun resetProgress() {
        val newProgress = UserProgress(
            completedLessons = emptyList(),
            totalScore = 0
        )
        saveUserProgress(newProgress)
    }

    // Chat Session Methods
    private fun loadChatSessionHistory() {
        val sessionHistoryJson = prefs.getString(KEY_CHAT_SESSION_HISTORY, null)
        val sessionHistory = sessionHistoryJson?.let { 
            gson.fromJson(it, ChatSessionHistory::class.java) 
        } ?: ChatSessionHistory()
        
        _chatSessionHistory.value = sessionHistory
        
        // Load current session if exists
        sessionHistory.currentSessionId?.let { currentId ->
            val currentSession = sessionHistory.sessions.find { it.id == currentId }
            _currentChatSession.value = currentSession
        }
    }

    // Tạo session mới cho lesson
    suspend fun startNewChatSession(lessonId: Int, lessonTitle: String) {
        val newSession = ChatSession(
            id = UUID.randomUUID().toString(),
            lessonId = lessonId,
            lessonTitle = lessonTitle,
            timestamp = System.currentTimeMillis()
        )
        
        _currentChatSession.value = newSession
        
        // Update session history với current session ID và thêm session mới vào danh sách
        // Không lưu session vào danh sách cho đến khi có tin nhắn
        // Điều này giúp không tạo quá nhiều session rỗng
        val updatedHistory = _chatSessionHistory.value.copy(
            currentSessionId = newSession.id
        )
        _chatSessionHistory.value = updatedHistory
        saveChatSessionHistory()
    }

    // Thêm message vào session hiện tại
    suspend fun addMessageToCurrentSession(message: String, isFromUser: Boolean) {
        val currentSession = _currentChatSession.value ?: return
        
        val chatMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            message = message,
            isFromUser = isFromUser
        )
        
        val updatedMessages = currentSession.messages + chatMessage
        val updatedSession = currentSession.copy(messages = updatedMessages)
        
        _currentChatSession.value = updatedSession
        
        // Update trong session history
        val currentHistory = _chatSessionHistory.value
        val updatedSessions = currentHistory.sessions.toMutableList()
        
        val existingIndex = updatedSessions.indexOfFirst { it.id == currentSession.id }
        if (existingIndex != -1) {
            updatedSessions[existingIndex] = updatedSession
        } else {
            updatedSessions.add(updatedSession)
        }
        
        val updatedHistory = currentHistory.copy(sessions = updatedSessions)
        _chatSessionHistory.value = updatedHistory
        saveChatSessionHistory()
    }

    // Load session cũ
    suspend fun loadChatSession(sessionId: String) {
        val session = _chatSessionHistory.value.sessions.find { it.id == sessionId }
        if (session != null) {
            _currentChatSession.value = session
            val updatedHistory = _chatSessionHistory.value.copy(currentSessionId = sessionId)
            _chatSessionHistory.value = updatedHistory
            saveChatSessionHistory()
        }
    }

    // Xóa session hiện tại
    suspend fun clearCurrentChatSession() {
        _currentChatSession.value = null
        val updatedHistory = _chatSessionHistory.value.copy(currentSessionId = null)
        _chatSessionHistory.value = updatedHistory
        saveChatSessionHistory()
    }

    // Xóa tất cả sessions
    suspend fun clearAllChatSessions() {
        _currentChatSession.value = null
        _chatSessionHistory.value = ChatSessionHistory()
        prefs.edit().remove(KEY_CHAT_SESSION_HISTORY).apply()
    }
    
    // Xóa một session cụ thể
    suspend fun deleteChatSession(sessionId: String) {
        val currentSessions = _chatSessionHistory.value.sessions
        val updatedSessions = currentSessions.filter { it.id != sessionId }
        
        // Nếu xóa session đang hiển thị, phải đặt current session = null
        if (_currentChatSession.value?.id == sessionId) {
            _currentChatSession.value = null
        }
        
        // Cập nhật lại session history
        val updatedHistory = _chatSessionHistory.value.copy(
            sessions = updatedSessions,
            currentSessionId = if (_chatSessionHistory.value.currentSessionId == sessionId) null
                             else _chatSessionHistory.value.currentSessionId
        )
        
        _chatSessionHistory.value = updatedHistory
        saveChatSessionHistory()
    }

    private fun saveChatSessionHistory() {
        val json = gson.toJson(_chatSessionHistory.value)
        prefs.edit().putString(KEY_CHAT_SESSION_HISTORY, json).apply()
    }

    suspend fun clearAllData() {
        prefs.edit().clear().apply()
        _userProfile.value = null
        _userProgress.value = null
        _currentChatSession.value = null
        _chatSessionHistory.value = ChatSessionHistory()
    }

    companion object {
        private const val PREFS_NAME = "crypto_learning_app"
        private const val KEY_NAME = "name"
        private const val KEY_GENDER = "gender"
        private const val KEY_BIRTH_YEAR = "birth_year"
        private const val KEY_USER_PROGRESS = "user_progress"
        private const val KEY_CHAT_SESSION_HISTORY = "chat_session_history"
    }
}
