package com.example.kidsstory.data.local

import android.content.Context
import com.example.kidsstory.data.local.model.StoryJson
import com.example.kidsstory.data.local.model.StorySegmentJson
import com.example.kidsstory.data.remote.ImageGenerationService
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
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
    private val gson: Gson,
    private val imageGenerationService: ImageGenerationService
) {

    /**
     * 讀取所有預設故事
     */
    fun loadPresetStories(): List<StoryJson> {
        val stories = mutableListOf<StoryJson>()

        try {
            val storyFiles = context.assets.list("stories") ?: emptyArray()

            storyFiles.forEach { fileName ->
                if (fileName.endsWith(".json")) {
                    try {
                        val jsonString = context.assets.open("stories/$fileName")
                            .bufferedReader()
                            .use { it.readText() }

                        var story = gson.fromJson(jsonString, StoryJson::class.java)

                        val coverImagePath = loadCoverImage(story.id)
                        if (coverImagePath != null) {
                            story = story.copy(coverImage = coverImagePath)
                        }

                        stories.add(story)
                    } catch (e: Exception) {
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
     * 讀取所有預設故事並載入段落圖片
     */
    fun loadPresetStoriesWithImages(): List<StoryJson> {
        val stories = mutableListOf<StoryJson>()

        try {
            val storyFiles = context.assets.list("stories") ?: emptyArray()

            storyFiles.forEach { fileName ->
                if (fileName.endsWith(".json")) {
                    try {
                        val jsonString = context.assets.open("stories/$fileName")
                            .bufferedReader()
                            .use { it.readText() }

                        var story = gson.fromJson(jsonString, StoryJson::class.java)

                        val coverImagePath = loadCoverImage(story.id)
                        if (coverImagePath != null) {
                            story = story.copy(coverImage = coverImagePath)
                        }

                        val segmentsWithImages = story.segments.map { segment ->
                            val imagePath = segment.image?.let { loadSegmentImage(story.id, it) }
                            segment.copy(image = imagePath)
                        }
                        story = story.copy(segments = segmentsWithImages)

                        stories.add(story)
                    } catch (e: Exception) {
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

            val assetsList = context.assets.list("images") ?: emptyArray()
            val coverFileName = "${storyId}_cover.png"

            if (!assetsList.contains(coverFileName)) {
                return null
            }

            val imagesDir = File(context.filesDir, "preset_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val targetFile = File(imagesDir, coverFileName)

            if (targetFile.exists()) {
                return targetFile.absolutePath
            }

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
     * 從 assets 載入段落圖片並複製到應用私有目錄
     */
    fun loadSegmentImage(storyId: String, segmentImageAsset: String): String? {
        return try {
            val assetPath = segmentImageAsset

            val assetsList = context.assets.list("images") ?: emptyArray()
            val fileName = segmentImageAsset.substringAfterLast("/")

            if (!assetsList.contains(fileName)) {
                return null
            }

            val imagesDir = File(context.filesDir, "preset_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val targetFile = File(imagesDir, fileName)

            if (targetFile.exists()) {
                return targetFile.absolutePath
            }

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

    /**
     * 為故事的所有段落生成圖片
     * 優化：改為 suspend 函數，在 IO dispatcher 中執行圖片生成
     * @param storyId 故事ID
     * @param segments 故事段落列表
     * @param characterRoleMap 段落索引到角色類型的映射
     * @return 更新後的段落列表（包含生成的圖片路徑）
     */
    suspend fun generateAllSegmentImages(
        storyId: String,
        segments: List<StorySegmentJson>,
        characterRoleMap: Map<Int, String>
    ): List<StorySegmentJson> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        segments.mapIndexed { index, segment ->
            val imagePath = generateSegmentImageIfNeeded(
                storyId = storyId,
                segmentIndex = index,
                segmentContent = segment.contentZh,
                characterRole = characterRoleMap[index] ?: "NARRATOR"
            )
            segment.copy(image = imagePath)
        }
    }

    /**
     * 為單個段落生成圖片（如果尚未生成）
     * 優化：改為 suspend 函數，避免使用 runBlocking 阻塞主線程
     */
    private suspend fun generateSegmentImageIfNeeded(
        storyId: String,
        segmentIndex: Int,
        segmentContent: String,
        characterRole: String
    ): String? {
        val imageFileName = "${storyId}_seg${segmentIndex + 1}_generated.png"
        val imagesDir = File(context.filesDir, "ai_generated_images")
        val targetFile = File(imagesDir, imageFileName)

        if (targetFile.exists()) {
            return targetFile.absolutePath
        }

        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        return try {
            imageGenerationService.generateSegmentImage(
                segmentContent = segmentContent,
                characterRole = characterRole,
                storyTheme = ""
            )
        } catch (e: Exception) {
            android.util.Log.e("PresetStoryDataSource", "Failed to generate segment image", e)
            null
        }
    }
}
