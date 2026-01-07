package com.example.kidsstory.data.repository

import com.example.kidsstory.data.database.dao.StoryDao
import com.example.kidsstory.data.database.dao.StorySegmentDao
import com.example.kidsstory.data.local.PresetStoryDataSource
import com.example.kidsstory.data.mapper.toDomain
import com.example.kidsstory.data.mapper.toEntity
import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.model.StoryCategory
import com.example.kidsstory.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 故事資料儲存庫實作
 */
@Singleton
class StoryRepositoryImpl @Inject constructor(
    private val storyDao: StoryDao,
    private val storySegmentDao: StorySegmentDao,
    private val presetStoryDataSource: PresetStoryDataSource
) : StoryRepository {

    override fun getAllStories(): Flow<List<Story>> {
        return storyDao.getAllStories().map { entities ->
            entities.map { entity ->
                val segments = storySegmentDao.getSegmentsByStoryId(entity.id)
                entity.toDomain(segments)
            }
        }
    }

    override suspend fun getStoryById(storyId: String): Story? {
        val entity = storyDao.getStoryById(storyId) ?: return null
        val segments = storySegmentDao.getSegmentsByStoryId(storyId)
        return entity.toDomain(segments)
    }

    override fun getStoriesByCategory(category: StoryCategory): Flow<List<Story>> {
        return storyDao.getStoriesByCategory(category.name).map { entities ->
            entities.map { entity ->
                val segments = storySegmentDao.getSegmentsByStoryId(entity.id)
                entity.toDomain(segments)
            }
        }
    }

    override fun getPresetStories(): Flow<List<Story>> {
        return storyDao.getPresetStories().map { entities ->
            entities.map { entity ->
                val segments = storySegmentDao.getSegmentsByStoryId(entity.id)
                entity.toDomain(segments)
            }
        }
    }

    override fun getAIGeneratedStories(): Flow<List<Story>> {
        return storyDao.getAIGeneratedStories().map { entities ->
            entities.map { entity ->
                val segments = storySegmentDao.getSegmentsByStoryId(entity.id)
                entity.toDomain(segments)
            }
        }
    }

    override suspend fun saveStory(story: Story) {
        // 儲存故事主體
        storyDao.insertStory(story.toEntity())

        // 儲存故事段落
        val segmentEntities = story.segments.map { it.toEntity(story.id) }
        storySegmentDao.insertSegments(segmentEntities)
    }

    override suspend fun updateLastPlayedAt(storyId: String, timestamp: Long) {
        storyDao.updateLastPlayedAt(storyId, timestamp)
    }

    override suspend fun initializePresetStories() {
        // 檢查是否已經初始化過
        val presetCount = storyDao.getPresetStoryCount()
        if (presetCount > 0) {
            return // 已經有資料，不需要重新初始化
        }

        // 從 assets 載入預設故事
        val presetStories = presetStoryDataSource.loadPresetStories()

        presetStories.forEach { storyJson ->
            // 儲存故事主體
            storyDao.insertStory(storyJson.toEntity())

            // 儲存故事段落
            val segments = storyJson.segments.map { it.toEntity(storyJson.id) }
            storySegmentDao.insertSegments(segments)
        }
    }
}
