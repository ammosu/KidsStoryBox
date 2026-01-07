package com.example.kidsstory

import android.app.Application
import com.example.kidsstory.domain.repository.StoryRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 應用程式主類
 * 使用 @HiltAndroidApp 啟用 Hilt 依賴注入
 */
@HiltAndroidApp
class KidsStoryApplication : Application() {

    @Inject
    lateinit var storyRepository: StoryRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // 初始化預設故事資料
        applicationScope.launch {
            try {
                storyRepository.initializePresetStories()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
