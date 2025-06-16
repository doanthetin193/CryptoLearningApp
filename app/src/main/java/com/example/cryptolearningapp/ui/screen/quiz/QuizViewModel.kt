package com.example.cryptolearningapp.ui.screen.quiz

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptolearningapp.data.model.Lesson
import com.example.cryptolearningapp.data.model.QuizQuestion
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
        val questions: List<QuizQuestion>,
        val currentQuestionIndex: Int = 0,
        val selectedAnswer: String? = null,
        val isAnswerSubmitted: Boolean = false,
        val isCorrect: Boolean = false,
        val correctAnswers: Int = 0,
        val finalScore: Int = 0
    ) : QuizState()
    data class Completed(
        val correctAnswers: Int,
        val finalScore: Int,
        val isLessonCompleted: Boolean
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

    companion object {
        const val CORRECT_ANSWER_POINTS = 10
        const val PARTIAL_CORRECT_POINTS = 6
        const val MIN_CORRECT_ANSWERS_FOR_COMPLETION = 3
    }

    fun loadQuiz(lessonId: Int) {
        currentLessonId = lessonId
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
        if (currentState is QuizState.Success && !currentState.isAnswerSubmitted) {
            val isCorrect = currentState.selectedAnswer == currentState.questions[currentState.currentQuestionIndex].answer
            val newCorrectAnswers = if (isCorrect) currentState.correctAnswers + 1 else currentState.correctAnswers

            _quizState.value = currentState.copy(
                isAnswerSubmitted = true,
                isCorrect = isCorrect,
                correctAnswers = newCorrectAnswers
            )
        }
    }

    fun nextQuestion() {
        val currentState = _quizState.value
        if (currentState is QuizState.Success) {
            if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
                _quizState.value = currentState.copy(
                    currentQuestionIndex = currentState.currentQuestionIndex + 1,
                    selectedAnswer = null,
                    isAnswerSubmitted = false,
                    isCorrect = false
                )
            } else {
                // Calculate final score
                val finalScore = when {
                    currentState.correctAnswers >= MIN_CORRECT_ANSWERS_FOR_COMPLETION -> CORRECT_ANSWER_POINTS
                    currentState.correctAnswers > 0 -> PARTIAL_CORRECT_POINTS
                    else -> 0
                }
                val isLessonCompleted = currentState.correctAnswers >= MIN_CORRECT_ANSWERS_FOR_COMPLETION

                // Update progress in repository
                updateProgress(finalScore, isLessonCompleted)

                // Update state to completed
                _quizState.value = QuizState.Completed(
                    correctAnswers = currentState.correctAnswers,
                    finalScore = finalScore,
                    isLessonCompleted = isLessonCompleted
                )
            }
        }
    }

    private fun updateProgress(finalScore: Int, isLessonCompleted: Boolean) {
        viewModelScope.launch {
            try {
                currentLessonId?.let { lessonId ->
                    val currentProgress = repository.getUserProgress("user1").first()
                    currentProgress?.let { progress ->
                        // Kiểm tra xem bài học đã hoàn thành chưa
                        val isAlreadyCompleted = progress.completedLessons.contains(lessonId)
                        
                        // Chỉ cập nhật điểm và trạng thái nếu bài học chưa hoàn thành
                        if (!isAlreadyCompleted) {
                            repository.updateProgress(
                                userId = "user1",
                                completedLessons = if (isLessonCompleted) {
                                    // Chỉ thêm vào completedLessons nếu đã trả lời đúng 3 câu
                                    progress.completedLessons + lessonId
                                } else {
                                    progress.completedLessons
                                },
                                totalScore = progress.totalScore + finalScore
                            )
                        }
                        // Nếu bài học đã hoàn thành, không cập nhật gì cả, chỉ hiển thị điểm
                    }
                }
            } catch (e: Exception) {
                _error.value = "Không thể cập nhật tiến độ: ${e.message}"
            }
        }
    }

    fun getCurrentQuestion(): QuizQuestion? {
        val currentState = _quizState.value
        return if (currentState is QuizState.Success) {
            currentState.questions[currentState.currentQuestionIndex]
        } else null
    }

    fun getProgress(): Float {
        val currentState = _quizState.value
        return if (currentState is QuizState.Success) {
            (currentState.currentQuestionIndex + 1).toFloat() / currentState.questions.size
        } else 0f
    }
} 