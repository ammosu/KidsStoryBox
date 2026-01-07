package com.example.kidsstory.di

import android.content.Context
import androidx.room.Room
import com.example.kidsstory.data.database.AppDatabase
import com.example.kidsstory.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 資料庫相關的依賴注入模組
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // 開發階段使用，正式版需要提供遷移策略
            .build()
    }

    @Provides
    @Singleton
    fun provideStoryDao(database: AppDatabase): StoryDao {
        return database.storyDao()
    }

    @Provides
    @Singleton
    fun provideStorySegmentDao(database: AppDatabase): StorySegmentDao {
        return database.storySegmentDao()
    }

    @Provides
    @Singleton
    fun provideCharacterDao(database: AppDatabase): CharacterDao {
        return database.characterDao()
    }

    @Provides
    @Singleton
    fun providePlayHistoryDao(database: AppDatabase): PlayHistoryDao {
        return database.playHistoryDao()
    }
}
