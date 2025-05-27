package com.example.cryptolearningapp.ui.screen.lesson

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptolearningapp.data.model.Lesson
import com.example.cryptolearningapp.data.repository.CryptoRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: CryptoRepository
) : ViewModel() {

    private val _lesson = MutableStateFlow<Lesson?>(null)
    val lesson: StateFlow<Lesson?> = _lesson

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadLesson(lessonId: Int) {
        viewModelScope.launch {
            try {
                val jsonString = context.assets.open("lessons.json").bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<Lesson>>() {}.type
                val lessonsList = Gson().fromJson<List<Lesson>>(jsonString, type)
                _lesson.value = lessonsList.find { it.id == lessonId }
            } catch (e: Exception) {
                _error.value = "Không thể tải bài học: ${e.message}"
            }
        }
    }
} 