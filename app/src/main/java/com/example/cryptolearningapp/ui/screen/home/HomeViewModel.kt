package com.example.cryptolearningapp.ui.screen.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptolearningapp.data.model.Lesson
import com.example.cryptolearningapp.data.model.UserProgress
import com.example.cryptolearningapp.data.repository.CryptoRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CryptoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons

    val userProgress: StateFlow<UserProgress?> = repository.getUserProgress("user1")

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadLessons()
    }

    private fun loadLessons() {
        viewModelScope.launch {
            try {
                val jsonString = context.assets.open("lessons.json").bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<Lesson>>() {}.type
                val lessonsList = Gson().fromJson<List<Lesson>>(jsonString, type)
                _lessons.value = lessonsList
            } catch (e: Exception) {
                _error.value = "Không thể tải danh sách bài học: ${e.message}"
            }
        }
    }

    fun updateProgress(lessonId: Int, score: Int) {
        viewModelScope.launch {
            try {
                val currentProgress = userProgress.value
                val completedLessons = currentProgress?.completedLessons?.toMutableList() ?: mutableListOf()
                if (!completedLessons.contains(lessonId)) {
                    completedLessons.add(lessonId)
                }
                val totalScore = (currentProgress?.totalScore ?: 0) + score
                repository.updateProgress("user1", completedLessons, totalScore)
            } catch (e: Exception) {
                _error.value = "Không thể cập nhật tiến độ: ${e.message}"
                }
        }
    }

    fun refreshData() {
        loadLessons()
    }
} 