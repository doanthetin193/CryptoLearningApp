package com.example.cryptolearningapp.ui

import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.cryptolearningapp.navigation.NavGraph
import com.example.cryptolearningapp.navigation.Screen

@Composable
fun MainScreen() {
    var isDarkMode by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    
    NavGraph(
        navController = navController,
        startDestination = Screen.Home.route,
        isDarkMode = isDarkMode,
        onThemeUpdated = { isDarkMode = it }
    )
} 