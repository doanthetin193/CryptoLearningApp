package com.example.cryptolearningapp.ui.screen.quiz

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptolearningapp.data.model.Lesson
import com.example.cryptolearningapp.data.model.Quiz
import com.example.cryptolearningapp.data.repository.CryptoRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QuizState {
    object Loading : QuizState()
    data class Success(
        val quiz: Quiz,
        val selectedAnswer: String? = null,
        val isAnswerSubmitted: Boolean = false,
        val isCorrect: Boolean = false,
        val scoreChange: Int = 0
    ) : QuizState()
    object Error : QuizState()
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: CryptoRepository
) : ViewModel() {

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Loading)
    val quizState: StateFlow<QuizState> = _quizState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentLessonId: Int? = null
    private var hasUpdatedScore = false

    companion object {
        const val CORRECT_ANSWER_POINTS = 10
        const val WRONG_ANSWER_PENALTY = -2
    }

    fun loadQuiz(lessonId: Int) {
        currentLessonId = lessonId
        hasUpdatedScore = false
        viewModelScope.launch {
            try {
                val jsonString = context.assets.open("lessons.json").bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<Lesson>>() {}.type
                val lessonsList = Gson().fromJson<List<Lesson>>(jsonString, type)
                val lesson = lessonsList.find { it.id == lessonId }
                if (lesson != null) {
                    _quizState.value = QuizState.Success(lesson.quiz)
                } else {
                    _quizState.value = QuizState.Error
                    _error.value = "Không tìm thấy bài học"
                }
            } catch (e: Exception) {
                _quizState.value = QuizState.Error
                _error.value = "Không thể tải quiz: ${e.message}"
            }
        }
    }

    fun selectAnswer(answer: String) {
        val currentState = _quizState.value
        if (currentState is QuizState.Success && !currentState.isAnswerSubmitted) {
            _quizState.value = currentState.copy(selectedAnswer = answer)
        }
    }

    fun submitAnswer() {
        val currentState = _quizState.value
        if (currentState is QuizState.Success && !currentState.isAnswerSubmitted && !hasUpdatedScore) {
            // Check if user has selected an answer
            if (currentState.selectedAnswer == null) {
                _error.value = "Vui lòng chọn một đáp án"
                return
            }

            val isCorrect = currentState.selectedAnswer == currentState.quiz.answer
            val scoreChange = if (isCorrect) CORRECT_ANSWER_POINTS else WRONG_ANSWER_PENALTY
            
            _quizState.value = currentState.copy(
                isAnswerSubmitted = true,
                isCorrect = isCorrect,
                scoreChange = scoreChange
            )

            viewModelScope.launch {
                try {
                    currentLessonId?.let { lessonId ->
                        // Get the current progress first
                        val currentProgress = repository.getUserProgress("user1").first()
                        currentProgress?.let { progress ->
                            // Only update score if the lesson is not completed yet
                            if (!progress.completedLessons.contains(lessonId)) {
                                val newCompletedLessons = if (isCorrect) {
                                    progress.completedLessons + lessonId
                                } else {
                                    progress.completedLessons
                                }
                                
                                val newScore = progress.totalScore + scoreChange
                                
                                repository.updateProgress(
                                    userId = "user1",
                                    completedLessons = newCompletedLessons,
                                    totalScore = newScore
                                )
                                hasUpdatedScore = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    _error.value = "Không thể cập nhật tiến độ: ${e.message}"
                }
            }
        }
    }
} 