package com.example.kidsstory.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.kidsstory.presentation.screens.ai_generation.AIGenerationScreen
import com.example.kidsstory.presentation.screens.library.StoryLibraryScreen
import com.example.kidsstory.presentation.screens.player.StoryPlayerScreen

/**
 * 應用程式導航圖
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.StoryLibrary.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 故事列表頁面
        composable(route = Screen.StoryLibrary.route) {
            StoryLibraryScreen(
                onStoryClick = { storyId ->
                    navController.navigate(Screen.StoryPlayer.createRoute(storyId))
                },
                onAIGenerationClick = {
                    navController.navigate(Screen.AIGeneration.route)
                }
            )
        }

        // 故事播放器頁面
        composable(
            route = Screen.StoryPlayer.route,
            arguments = listOf(
                navArgument("storyId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId") ?: return@composable
            StoryPlayerScreen(
                storyId = storyId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // AI故事生成頁面
        composable(route = Screen.AIGeneration.route) {
            AIGenerationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStoryGenerated = { storyId ->
                    // 生成完成後導航到播放器
                    navController.navigate(Screen.StoryPlayer.createRoute(storyId)) {
                        popUpTo(Screen.StoryLibrary.route)
                    }
                }
            )
        }
    }
}
