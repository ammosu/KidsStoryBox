package com.example.kidsstory.domain.usecases

import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.repository.StoryRepository
import javax.inject.Inject

/**
 * 依 ID 取得故事
 */
class GetStoryByIdUseCase @Inject constructor(
    private val storyRepository: StoryRepository
) {
    suspend operator fun invoke(storyId: String): Story? {
        return storyRepository.getStoryById(storyId)
    }
}
