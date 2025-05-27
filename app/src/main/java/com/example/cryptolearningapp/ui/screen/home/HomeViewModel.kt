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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CryptoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons

    private val _userProgress = MutableStateFlow<UserProgress?>(null)
    val userProgress: StateFlow<UserProgress?> = _userProgress

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadLessons()
        loadUserProgress()
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

    private fun loadUserProgress() {
        viewModelScope.launch {
            repository.getUserProgress("user1")
                .catch { e ->
                    _error.value = "Không thể tải tiến độ học tập: ${e.message}"
                }
                .collect { progress ->
                    _userProgress.value = progress
                }
        }
    }

    fun refreshData() {
        loadLessons()
        loadUserProgress()
    }
} 