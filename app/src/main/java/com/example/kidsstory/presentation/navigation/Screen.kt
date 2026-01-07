package com.example.kidsstory.presentation.navigation

/**
 * 導航路由定義
 */
sealed class Screen(val route: String) {
    object StoryLibrary : Screen("story_library")
    object StoryPlayer : Screen("story_player/{storyId}") {
        fun createRoute(storyId: String) = "story_player/$storyId"
    }
    object AIGeneration : Screen("ai_generation")
}
