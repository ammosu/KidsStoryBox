package com.example.kidsstory.data.database.dao

import androidx.room.*
import com.example.kidsstory.data.database.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 故事資料存取物件
 */
@Dao
interface StoryDao {

    @Query("SELECT * FROM stories ORDER BY lastPlayedAt DESC, createdAt DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE id = :storyId")
    suspend fun getStoryById(storyId: String): StoryEntity?

    @Query("SELECT * FROM stories WHERE id = :storyId")
    fun getStoryByIdFlow(storyId: String): Flow<StoryEntity?>

    @Query("SELECT * FROM stories WHERE category = :category ORDER BY createdAt DESC")
    fun getStoriesByCategory(category: String): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE isPreset = 1 ORDER BY createdAt ASC")
    fun getPresetStories(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE isPreset = 0 ORDER BY createdAt DESC")
    fun getAIGeneratedStories(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT :limit")
    suspend fun getRecentlyPlayed(limit: Int): List<StoryEntity>

    @Query("SELECT * FROM stories WHERE isDownloaded = 1")
    fun getDownloadedStories(): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Update
    suspend fun updateStory(story: StoryEntity)

    @Query("UPDATE stories SET lastPlayedAt = :timestamp WHERE id = :storyId")
    suspend fun updateLastPlayedAt(storyId: String, timestamp: Long)

    @Query("UPDATE stories SET isDownloaded = :isDownloaded WHERE id = :storyId")
    suspend fun updateDownloadStatus(storyId: String, isDownloaded: Boolean)

    @Delete
    suspend fun deleteStory(story: StoryEntity)

    @Query("DELETE FROM stories WHERE id = :storyId")
    suspend fun deleteStoryById(storyId: String)
}
