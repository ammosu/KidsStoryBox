package com.example.kidsstory.domain.usecases

import com.example.kidsstory.domain.repository.StoryRepository
import javax.inject.Inject

/**
 * 更新最後播放時間
 */
class UpdateLastPlayedAtUseCase @Inject constructor(
    private val storyRepository: StoryRepository
) {
    suspend operator fun invoke(storyId: String, timestamp: Long) {
        storyRepository.updateLastPlayedAt(storyId, timestamp)
    }
}
