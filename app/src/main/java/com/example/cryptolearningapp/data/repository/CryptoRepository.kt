package com.example.cryptolearningapp.data.repository

import com.example.cryptolearningapp.data.dao.UserDao
import com.example.cryptolearningapp.data.dao.UserProgressDao
import com.example.cryptolearningapp.data.model.User
import com.example.cryptolearningapp.data.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoRepository @Inject constructor(
    private val userDao: UserDao,
    private val userProgressDao: UserProgressDao
) {
    private val _progressUpdateEvent = MutableSharedFlow<Unit>()
    val progressUpdateEvent: SharedFlow<Unit> = _progressUpdateEvent

    fun getUser(userId: String): Flow<User?> = userDao.getUserById(userId)

    suspend fun createUser(user: User) {
        userDao.insertUser(user)
        // Tạo UserProgress mặc định cho user mới
        userProgressDao.insertUserProgress(
            UserProgress(
                userId = user.id,
                completedLessons = emptyList(),
                totalScore = 0
            )
        )
    }

    fun getUserProgress(userId: String): Flow<UserProgress?> = userProgressDao.getUserProgress(userId)

    suspend fun updateUserProgress(userId: String, completedLessons: List<Int>, totalScore: Int) {
        userProgressDao.updateProgress(userId, completedLessons, totalScore)
        _progressUpdateEvent.emit(Unit)
    }
} 