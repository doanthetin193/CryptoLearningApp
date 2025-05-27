package com.example.cryptolearningapp.ui.screen.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cryptolearningapp.data.model.Gender
import com.example.cryptolearningapp.data.model.UserProfile
import kotlinx.coroutines.launch
import java.time.Year

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    var birthYear by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val currentYear = Year.now().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Chào mừng bạn đến với Crypto Learning App",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Name input
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên của bạn") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Gender selection
        Text(
            text = "Giới tính",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Gender.values().forEach { gender ->
                FilterChip(
                    selected = selectedGender == gender,
                    onClick = { selectedGender = gender },
                    label = {
                        Text(
                            when (gender) {
                                Gender.MALE -> "Nam"
                                Gender.FEMALE -> "Nữ"
                                Gender.OTHER -> "Khác"
                            }
                        )
                    }
                )
            }
        }

        // Birth year input
        OutlinedTextField(
            value = birthYear,
            onValueChange = { 
                if (it.isEmpty() || it.toIntOrNull() != null) {
                    birthYear = it
                }
            },
            label = { Text("Năm sinh") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        if (showError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue button
        Button(
            onClick = {
                when {
                    name.isBlank() -> {
                        showError = true
                        errorMessage = "Vui lòng nhập tên của bạn"
                    }
                    selectedGender == null -> {
                        showError = true
                        errorMessage = "Vui lòng chọn giới tính"
                    }
                    birthYear.isBlank() -> {
                        showError = true
                        errorMessage = "Vui lòng nhập năm sinh"
                    }
                    birthYear.toIntOrNull() == null -> {
                        showError = true
                        errorMessage = "Năm sinh không hợp lệ"
                    }
                    birthYear.toInt() < 1900 || birthYear.toInt() > currentYear -> {
                        showError = true
                        errorMessage = "Năm sinh phải từ 1900 đến $currentYear"
                    }
                    else -> {
                        showError = false
                        scope.launch {
                            viewModel.saveUserProfile(
                                UserProfile(
                                    name = name,
                                    gender = selectedGender!!,
                                    birthYear = birthYear.toInt()
                                )
                            )
                            onComplete()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Tiếp tục")
        }
    }
} 