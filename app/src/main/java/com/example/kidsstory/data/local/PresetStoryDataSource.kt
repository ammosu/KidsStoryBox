package com.example.kidsstory.data.local

import android.content.Context
import com.example.kidsstory.data.local.model.StoryJson
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 預設故事資料源 - 從 assets 讀取 JSON 檔案
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

                        val story = gson.fromJson(jsonString, StoryJson::class.java)
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
