package com.example.kidsstory.data.repository

import com.example.kidsstory.data.database.dao.StoryDao
import com.example.kidsstory.data.database.dao.StorySegmentDao
import com.example.kidsstory.data.local.PresetStoryDataSource
import com.example.kidsstory.data.local.model.StorySegmentJson
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
    private val storySegmentDao: StorySegmentDao,
    private val presetStoryDataSource: PresetStoryDataSource
) : StoryRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
