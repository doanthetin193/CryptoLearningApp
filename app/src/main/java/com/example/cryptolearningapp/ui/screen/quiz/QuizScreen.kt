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
                is QuizState.Success -> {
                    val state = quizState as QuizState.Success
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Question Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = "Câu hỏi",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.quiz.question,
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Answer Options
                        state.quiz.options.forEach { option ->
                            val isSelected = option == state.selectedAnswer
                            val isCorrect = state.isAnswerSubmitted && option == state.quiz.answer
                            val isWrong = state.isAnswerSubmitted && isSelected && option != state.quiz.answer

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
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = when {
                                            isCorrect || isWrong -> Color.White
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                    if (state.isAnswerSubmitted) {
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                            contentDescription = if (isCorrect) "Correct" else "Wrong",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Result and Score Change
                        AnimatedVisibility(
                            visible = state.isAnswerSubmitted,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (state.isCorrect) 
                                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    else 
                                        Color(0xFFE57373).copy(alpha = 0.1f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (state.isCorrect) "Chính xác!" else "Chưa chính xác",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = if (state.isCorrect) 
                                            Color(0xFF4CAF50)
                                        else 
                                            Color(0xFFE57373)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (state.scoreChange > 0) 
                                            "+${state.scoreChange} điểm"
                                        else 
                                            "${state.scoreChange} điểm",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (state.scoreChange > 0) 
                                            Color(0xFF4CAF50)
                                        else 
                                            Color(0xFFE57373)
                                    )
                                }
                            }
                        }

                        // Action Button
                        Button(
                            onClick = {
                                if (state.isAnswerSubmitted) {
                                    onFinishQuiz()
                                } else {
                                    viewModel.submitAnswer()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isAnswerSubmitted)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(
                                text = if (state.isAnswerSubmitted) "Tiếp tục" else "Kiểm tra",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                is QuizState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Có lỗi xảy ra",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
} 