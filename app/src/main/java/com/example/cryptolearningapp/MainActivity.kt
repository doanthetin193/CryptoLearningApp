package com.example.cryptolearningapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.cryptolearningapp.navigation.NavGraph
import com.example.cryptolearningapp.navigation.Screen
import com.example.cryptolearningapp.ui.theme.CryptoLearningAppTheme
import com.example.cryptolearningapp.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
            val navController = rememberNavController()
            var isDarkMode by remember { mutableStateOf(false) }

            CryptoLearningAppTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        navController = navController,
                        startDestination = if (isOnboardingCompleted) Screen.Home.route else Screen.Onboarding.route,
                        isDarkMode = isDarkMode,
                        onThemeUpdated = { isDarkMode = it }
                    )
                }
            }
        }
    }
}