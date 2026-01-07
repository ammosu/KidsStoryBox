package com.example.kidsstory.data.database.dao

import androidx.room.*
import com.example.kidsstory.data.database.entity.StorySegmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * 故事段落資料存取物件
 */
@Dao
interface StorySegmentDao {

    @Query("SELECT * FROM story_segments WHERE storyId = :storyId ORDER BY sequenceNumber ASC")
    suspend fun getSegmentsByStoryId(storyId: String): List<StorySegmentEntity>

    @Query("SELECT * FROM story_segments WHERE storyId = :storyId ORDER BY sequenceNumber ASC")
    fun getSegmentsByStoryIdFlow(storyId: String): Flow<List<StorySegmentEntity>>

    @Query("SELECT * FROM story_segments WHERE id = :segmentId")
    suspend fun getSegmentById(segmentId: Long): StorySegmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegment(segment: StorySegmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<StorySegmentEntity>)

    @Update
    suspend fun updateSegment(segment: StorySegmentEntity)

    @Query("UPDATE story_segments SET audioPathZh = :audioPath WHERE storyId = :storyId AND sequenceNumber = :sequenceNumber")
    suspend fun updateAudioPathZh(storyId: String, sequenceNumber: Int, audioPath: String)

    @Query("UPDATE story_segments SET audioPathEn = :audioPath WHERE storyId = :storyId AND sequenceNumber = :sequenceNumber")
    suspend fun updateAudioPathEn(storyId: String, sequenceNumber: Int, audioPath: String)

    @Delete
    suspend fun deleteSegment(segment: StorySegmentEntity)

    @Query("DELETE FROM story_segments WHERE storyId = :storyId")
    suspend fun deleteSegmentsByStoryId(storyId: String)
}
