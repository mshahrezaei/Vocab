package com.vocabmaster.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vocabmaster.ui.screens.*

object Routes {
    const val HOME = "home"
    const val LESSONS = "lessons"
    const val FLASHCARD = "flashcard/{lessonId}"
    const val WEAK_WORDS = "weak"
    const val STATS = "stats"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val REVIEW_TODAY = "review_today"

    fun flashcard(lessonId: Long) = "flashcard/$lessonId"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(navController = navController) }
        composable(Routes.LESSONS) { LessonListScreen(navController = navController) }
        composable(Routes.FLASHCARD) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId")?.toLongOrNull() ?: 0L
            FlashcardScreen(lessonId = lessonId, navController = navController)
        }
        composable(Routes.WEAK_WORDS) { WeakWordsScreen(navController = navController) }
        composable(Routes.STATS) { StatsScreen(navController = navController) }
        composable(Routes.SEARCH) { SearchScreen(navController = navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController = navController) }
        composable(Routes.REVIEW_TODAY) { FlashcardScreen(lessonId = -1L, navController = navController) }
    }
}
