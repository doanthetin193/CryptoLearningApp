package com.example.cryptolearningapp.ui.screen.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    lessonId: Int,
    onBackClick: () -> Unit,
    onFinishQuiz: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val quizState by viewModel.quizState.collectAsState()

    LaunchedEffect(lessonId) {
        viewModel.loadQuiz(lessonId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (quizState) {
                is QuizState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is QuizState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Có lỗi xảy ra khi tải quiz")
                    }
                }
                is QuizState.Completed -> {
                    val completedState = quizState as QuizState.Completed
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                if (completedState.isLessonCompleted) 
                                    "Chúc mừng! Bạn đã hoàn thành bài học"
                                else 
                                    "Bạn đã hoàn thành quiz",
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                            Text(
                                "Số câu trả lời đúng: ${completedState.correctAnswers}/3",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(
                                "Điểm nhận được: ${completedState.finalScore}",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(8.dp)
                            )
                            if (!completedState.isLessonCompleted) {
                                Text(
                                    "Bạn cần trả lời đúng ít nhất 3 câu để hoàn thành bài học",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            Button(
                                onClick = onFinishQuiz,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("Quay lại bài học")
                            }
                        }
                    }
                }
                is QuizState.Success -> {
                    val state = quizState as QuizState.Success
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Progress bar
                        LinearProgressIndicator(
                            progress = viewModel.getProgress(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        // Question number
                        Text(
                            "Câu hỏi ${state.currentQuestionIndex + 1}/${state.questions.size}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Question
                        Text(
                            state.questions[state.currentQuestionIndex].question,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Options
                        state.questions[state.currentQuestionIndex].options.forEach { option ->
                            val isSelected = option == state.selectedAnswer
                            val isCorrect = state.isAnswerSubmitted && option == state.questions[state.currentQuestionIndex].answer
                            val isWrong = state.isAnswerSubmitted && isSelected && option != state.questions[state.currentQuestionIndex].answer

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isCorrect -> Color(0xFF4CAF50) // Green
                                        isWrong -> Color(0xFFE57373) // Red
                                        isSelected -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                onClick = {
                                    if (!state.isAnswerSubmitted) {
                                        viewModel.selectAnswer(option)
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        option,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (state.isAnswerSubmitted) {
                                        Icon(
                                            imageVector = when {
                                                isCorrect -> Icons.Default.Check
                                                isWrong -> Icons.Default.Close
                                                else -> Icons.Default.Check
                                            },
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Submit/Next button
                        Button(
                            onClick = {
                                if (state.isAnswerSubmitted) {
                                    viewModel.nextQuestion()
                                } else {
                                    viewModel.submitAnswer()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Text(if (state.isAnswerSubmitted) "Câu tiếp theo" else "Kiểm tra")
                        }
                    }
                }
            }
        }
    }
} 