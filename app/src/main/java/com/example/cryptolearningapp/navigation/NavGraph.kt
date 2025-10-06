package com.example.cryptolearningapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cryptolearningapp.ui.news.NewsScreen
import com.example.cryptolearningapp.ui.screen.chart.ChartScreen
import com.example.cryptolearningapp.ui.screen.home.HomeScreen
import com.example.cryptolearningapp.ui.screen.lesson.LessonScreen
import com.example.cryptolearningapp.ui.screen.onboarding.OnboardingScreen
import com.example.cryptolearningapp.ui.screen.profile.ProfileScreen
import com.example.cryptolearningapp.ui.screen.quiz.QuizScreen
import com.example.cryptolearningapp.ui.screen.editprofile.EditProfileScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Lesson : Screen("lesson/{lessonId}") {
        fun createRoute(lessonId: Int) = "lesson/$lessonId"
    }
    object Quiz : Screen("quiz/{lessonId}") {
        fun createRoute(lessonId: Int) = "quiz/$lessonId"
    }
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object News : Screen("news")
    object Chart : Screen("chart")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    isDarkMode: Boolean,
    onThemeUpdated: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onLessonClick = { lessonId ->
                    navController.navigate(Screen.Lesson.createRoute(lessonId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onNewsClick = {
                    navController.navigate(Screen.News.route)
                },
                onChartClick = {
                    navController.navigate(Screen.Chart.route)
                },
                isDarkMode = isDarkMode,
                onThemeUpdated = onThemeUpdated
            )
        }

        composable(Screen.Lesson.route) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId")?.toIntOrNull() ?: return@composable
            LessonScreen(
                lessonId = lessonId,
                onBackClick = { navController.popBackStack() },
                onQuizClick = { navController.navigate(Screen.Quiz.createRoute(lessonId)) }
            )
        }

        composable(Screen.Quiz.route) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId")?.toIntOrNull() ?: return@composable
            QuizScreen(
                lessonId = lessonId,
                onBackClick = { navController.popBackStack() },
                onFinishQuiz = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onEditProfileClick = {
                    navController.navigate(Screen.EditProfile.route)
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.News.route) {
            NewsScreen()
        }

        composable(Screen.Chart.route) {
            ChartScreen()
        }
    }
} 