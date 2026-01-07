package com.example.kidsstory.domain.usecases

import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.repository.StoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * 取得所有故事
 */
class GetAllStoriesUseCase @Inject constructor(
    private val storyRepository: StoryRepository
) {
    operator fun invoke(): Flow<List<Story>> = storyRepository.getAllStories()
}
