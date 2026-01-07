package com.example.kidsstory.domain.usecases

import com.example.kidsstory.domain.repository.StoryRepository
import javax.inject.Inject

/**
 * 初始化預設故事資料
 */
class InitializePresetStoriesUseCase @Inject constructor(
    private val storyRepository: StoryRepository
) {
    suspend operator fun invoke() {
        storyRepository.initializePresetStories()
    }
}
