package com.example.kidsstory.data.database.dao

import androidx.room.*
import com.example.kidsstory.data.database.entity.PlayHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 播放歷史資料存取物件
 */
@Dao
interface PlayHistoryDao {

    @Query("SELECT * FROM play_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<PlayHistoryEntity>>

    @Query("SELECT * FROM play_history WHERE storyId = :storyId ORDER BY playedAt DESC LIMIT 1")
    suspend fun getLastPlayHistory(storyId: String): PlayHistoryEntity?

    @Query("SELECT * FROM play_history WHERE storyId = :storyId ORDER BY playedAt DESC")
    fun getHistoryByStoryId(storyId: String): Flow<List<PlayHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PlayHistoryEntity): Long

    @Update
    suspend fun updateHistory(history: PlayHistoryEntity)

    @Query("DELETE FROM play_history WHERE playedAt < :timestamp")
    suspend fun deleteOldHistory(timestamp: Long)

    @Query("DELETE FROM play_history")
    suspend fun clearAllHistory()
}
