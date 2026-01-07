package com.example.kidsstory.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kidsstory.data.database.dao.*
import com.example.kidsstory.data.database.entity.*

/**
 * 應用程式主要資料庫
 * 包含故事、段落、角色和播放歷史
 */
@Database(
    entities = [
        StoryEntity::class,
        StorySegmentEntity::class,
        CharacterEntity::class,
        PlayHistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun storyDao(): StoryDao
    abstract fun storySegmentDao(): StorySegmentDao
    abstract fun characterDao(): CharacterDao
    abstract fun playHistoryDao(): PlayHistoryDao

    companion object {
        const val DATABASE_NAME = "kids_story_database"
    }
}
