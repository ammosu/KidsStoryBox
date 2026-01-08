package com.example.kidsstory.data.local

import android.content.Context
import com.example.kidsstory.data.local.model.StoryJson
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 預設故事資料源 - 從 assets 讀取 JSON 檔案和圖片
 */
@Singleton
class PresetStoryDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {

    /**
     * 讀取所有預設故事
     */
    fun loadPresetStories(): List<StoryJson> {
        val stories = mutableListOf<StoryJson>()

        try {
            // 列出 assets/stories 目錄下的所有 JSON 檔案
            val storyFiles = context.assets.list("stories") ?: emptyArray()

            storyFiles.forEach { fileName ->
                if (fileName.endsWith(".json")) {
                    try {
                        val jsonString = context.assets.open("stories/$fileName")
                            .bufferedReader()
                            .use { it.readText() }

                        var story = gson.fromJson(jsonString, StoryJson::class.java)

                        // 嘗試載入封面圖片
                        val coverImagePath = loadCoverImage(story.id)
                        if (coverImagePath != null) {
                            story = story.copy(coverImage = coverImagePath)
                        }

                        stories.add(story)
                    } catch (e: Exception) {
                        // 記錄錯誤但繼續載入其他故事
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return stories
    }

    /**
     * 從 assets 載入封面圖片並複製到應用私有目錄
     */
    private fun loadCoverImage(storyId: String): String? {
        return try {
            val assetPath = "images/${storyId}_cover.png"

            // 檢查 assets 中是否存在該圖片
            val assetsList = context.assets.list("images") ?: emptyArray()
            val coverFileName = "${storyId}_cover.png"

            if (!assetsList.contains(coverFileName)) {
                return null
            }

            // 創建目標目錄
            val imagesDir = File(context.filesDir, "preset_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            // 目標檔案
            val targetFile = File(imagesDir, coverFileName)

            // 如果檔案已存在，直接返回路徑
            if (targetFile.exists()) {
                return targetFile.absolutePath
            }

            // 從 assets 複製圖片到私有目錄
            context.assets.open(assetPath).use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 根據 ID 讀取特定故事
     */
    fun loadStoryById(storyId: String): StoryJson? {
        return try {
            val jsonString = context.assets.open("stories/$storyId.json")
                .bufferedReader()
                .use { it.readText() }

            gson.fromJson(jsonString, StoryJson::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
