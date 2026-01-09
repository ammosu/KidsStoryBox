package com.example.kidsstory.data.repository

import com.example.kidsstory.data.database.dao.StoryDao
import com.example.kidsstory.data.database.dao.StorySegmentDao
import com.example.kidsstory.data.local.PresetStoryDataSource
import com.example.kidsstory.data.mapper.toDomain
import com.example.kidsstory.data.mapper.toEntity
import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.model.StoryCategory
import com.example.kidsstory.domain.repository.StoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRepositoryImpl @Inject constructor(
    private val storyDao: StoryDao,
    private val storySegmentDao: StorySegmentDao,  // 仍需要用於 saveStory 和 initializePresetStories
    private val presetStoryDataSource: PresetStoryDataSource
) : StoryRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 獲取所有故事
     * 優化：使用 @Transaction 和 @Relation 避免 N+1 查詢問題
     */
    override fun getAllStories(): Flow<List<Story>> {
        return storyDao.getAllStoriesWithSegments().map { storiesWithSegments ->
            storiesWithSegments.map { it.toDomain() }
        }
    }

    /**
     * 根據 ID 獲取故事
     * 優化：使用單一查詢獲取故事和段落
     */
    override suspend fun getStoryById(storyId: String): Story? {
        return storyDao.getStoryByIdWithSegments(storyId)?.toDomain()
    }

    /**
     * 根據分類獲取故事
     * 優化：使用 @Transaction 和 @Relation 避免 N+1 查詢問題
     */
    override fun getStoriesByCategory(category: StoryCategory): Flow<List<Story>> {
        return storyDao.getStoriesByCategoryWithSegments(category.name).map { storiesWithSegments ->
            storiesWithSegments.map { it.toDomain() }
        }
    }

    /**
     * 獲取預設故事
     * 優化：使用 @Transaction 和 @Relation 避免 N+1 查詢問題
     */
    override fun getPresetStories(): Flow<List<Story>> {
        return storyDao.getPresetStoriesWithSegments().map { storiesWithSegments ->
            storiesWithSegments.map { it.toDomain() }
        }
    }

    /**
     * 獲取 AI 生成的故事
     * 優化：使用 @Transaction 和 @Relation 避免 N+1 查詢問題
     */
    override fun getAIGeneratedStories(): Flow<List<Story>> {
        return storyDao.getAIGeneratedStoriesWithSegments().map { storiesWithSegments ->
            storiesWithSegments.map { it.toDomain() }
        }
    }

    override suspend fun saveStory(story: Story) {
        storyDao.insertStory(story.toEntity())

        val segmentEntities = story.segments.map { it.toEntity(story.id) }
        storySegmentDao.insertSegments(segmentEntities)
    }

    override suspend fun updateLastPlayedAt(storyId: String, timestamp: Long) {
        storyDao.updateLastPlayedAt(storyId, timestamp)
    }

    override suspend fun initializePresetStories() {
        val presetCount = storyDao.getPresetStoryCount()
        if (presetCount > 0) {
            return
        }

        val presetStories = presetStoryDataSource.loadPresetStories()

        presetStories.forEach { storyJson ->
            storyDao.insertStory(storyJson.toEntity())

            val segments = storyJson.segments.map { segment ->
                val imagePath = segment.image?.let {
                    presetStoryDataSource.loadSegmentImage(storyJson.id, it)
                }
                segment.toEntity(storyJson.id, imagePath)
            }
            storySegmentDao.insertSegments(segments)
        }
    }
}
