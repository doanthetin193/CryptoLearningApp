package com.example.cryptolearningapp.ui.screen.quiz

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptolearningapp.data.model.Lesson
import com.example.cryptolearningapp.data.model.QuizQuestion
import com.example.cryptolearningapp.data.repository.UserRepository
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
    private val repository: UserRepository
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
                val finalScore = calculateScore()
                val isLessonCompleted = currentState.correctAnswers >= MIN_CORRECT_ANSWERS_FOR_COMPLETION

                // Update progress in repository
                updateProgress()

                // Update state to completed
                _quizState.value = QuizState.Completed(
                    correctAnswers = currentState.correctAnswers,
                    finalScore = finalScore,
                    isLessonCompleted = isLessonCompleted
                )
            }
        }
    }

    private fun calculateScore(): Int {
        val currentState = _quizState.value
        if (currentState !is QuizState.Success) return 0
        
        val correctAnswers = currentState.correctAnswers
        val totalQuestions = currentState.questions.size
        val wrongAnswers = totalQuestions - correctAnswers
        
        // Điểm thưởng cho câu đúng
        val correctPoints = when (correctAnswers) {
            3 -> CORRECT_ANSWER_POINTS  // Đúng cả 3 câu: 10 điểm
            2 -> PARTIAL_CORRECT_POINTS  // Đúng 2 câu: 6 điểm
            1 -> 3   // Đúng 1 câu: 3 điểm
            else -> 0 // Không đúng câu nào: 0 điểm
        }
        
        // Trừ điểm cho câu sai (mỗi câu sai trừ 2 điểm)
        val penaltyPoints = wrongAnswers * 2
        
        // Tổng điểm = điểm thưởng - điểm trừ
        return correctPoints - penaltyPoints // Bỏ coerceAtLeast để cho phép điểm âm
    }

    private fun updateProgress() {
        viewModelScope.launch {
            try {
                currentLessonId?.let { lessonId ->
                    val currentProgress = repository.userProgress.first()
                    currentProgress?.let { progress ->
                        val currentState = _quizState.value
                        if (currentState is QuizState.Success) {
                            val finalScore = calculateScore()
                            val isLessonCompleted = currentState.correctAnswers >= MIN_CORRECT_ANSWERS_FOR_COMPLETION
                            
                            // Kiểm tra xem bài học đã hoàn thành chưa
                            val isAlreadyCompleted = progress.completedLessons.contains(lessonId)
                            
                            if (!isAlreadyCompleted) {
                                // Nếu bài học chưa hoàn thành, cập nhật điểm và trạng thái
                                repository.updateProgress(
                                    completedLessons = if (isLessonCompleted) {
                                        progress.completedLessons + lessonId
                                    } else {
                                        progress.completedLessons
                                    },
                                    totalScore = progress.totalScore + finalScore
                                )
                            } else {
                                // Nếu bài học đã hoàn thành, chỉ cập nhật điểm (có thể trừ điểm)
                                val newTotalScore = progress.totalScore + finalScore
                                repository.updateProgress(
                                    completedLessons = progress.completedLessons,
                                    totalScore = newTotalScore.coerceAtLeast(0) // Đảm bảo tổng điểm không âm
                                )
                            }
                        }
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