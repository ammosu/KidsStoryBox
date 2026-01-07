package com.example.kidsstory.domain.repository

import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.model.StoryCategory
import kotlinx.coroutines.flow.Flow

/**
 * 故事資料儲存庫介面
 */
interface StoryRepository {

    /**
     * 取得所有故事
     */
    fun getAllStories(): Flow<List<Story>>

    /**
     * 根據 ID 取得故事
     */
    suspend fun getStoryById(storyId: String): Story?

    /**
     * 根據分類取得故事
     */
    fun getStoriesByCategory(category: StoryCategory): Flow<List<Story>>

    /**
     * 取得預設故事
     */
    fun getPresetStories(): Flow<List<Story>>

    /**
     * 取得 AI 生成的故事
     */
    fun getAIGeneratedStories(): Flow<List<Story>>

    /**
     * 儲存故事
     */
    suspend fun saveStory(story: Story)

    /**
     * 更新最後播放時間
     */
    suspend fun updateLastPlayedAt(storyId: String, timestamp: Long)

    /**
     * 初始化預設故事資料
     */
    suspend fun initializePresetStories()
}
