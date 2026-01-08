package com.example.kidsstory.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.kidsstory.domain.model.StoryCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 圖片生成服務
 * 整合本地 Z-Image-Turbo API (http://localhost:7860)
 */
@Singleton
class ImageGenerationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Android 模擬器訪問主機 localhost 需要使用 10.0.2.2
    // 如果是真實設備，請改為實際 IP，例如 "http://192.168.1.100:7860"
    private val baseUrl = "http://10.0.2.2:7860"

    /**
     * 為故事生成封面圖
     * @param storyTitle 故事標題
     * @param category 故事分類
     * @param protagonist 主角（可選）
     * @param language 語言（zh 或 en）
     * @return 圖片的本地檔案路徑，失敗返回 null
     */
    suspend fun generateStoryCover(
        storyTitle: String,
        category: StoryCategory,
        protagonist: String = "",
        language: String = "zh"
    ): String? = withContext(Dispatchers.IO) {
        try {
            val prompt = buildStoryCoverPrompt(storyTitle, category, protagonist, language)
            val imageData = callImageGenerationAPI(prompt)

            if (imageData != null) {
                saveImageToFile(imageData, "story_cover_${System.currentTimeMillis()}.png")
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 為故事段落生成配圖
     * @param segmentContent 段落內容
     * @param characterRole 角色類型
     * @param storyTheme 故事主題
     * @return 圖片的本地檔案路徑
     */
    suspend fun generateSegmentImage(
        segmentContent: String,
        characterRole: String,
        storyTheme: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val prompt = buildSegmentImagePrompt(segmentContent, characterRole, storyTheme)
            val imageData = callImageGenerationAPI(prompt)

            if (imageData != null) {
                saveImageToFile(imageData, "segment_${System.currentTimeMillis()}.png")
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 構建故事封面 Prompt
     */
    private fun buildStoryCoverPrompt(
        storyTitle: String,
        category: StoryCategory,
        protagonist: String,
        language: String
    ): String {
        val baseStyle = "children's book illustration, cute and colorful, soft pastel colors, " +
                "whimsical art style, friendly and safe for kids ages 3-6, " +
                "digital art, high quality, detailed"

        val categoryStyle = when (category) {
            StoryCategory.FRIENDSHIP -> "happy children playing together, warm atmosphere"
            StoryCategory.ADVENTURE -> "exciting journey, magical landscape, discovery"
            StoryCategory.FAMILY -> "loving family scene, cozy home, heartwarming"
            StoryCategory.MORAL -> "kindness and caring, positive message, gentle scene"
            StoryCategory.FANTASY -> "magical creatures, enchanted forest, dreamlike"
            StoryCategory.SCIENCE -> "space or nature exploration, wonder and curiosity"
            StoryCategory.NATURE -> "beautiful natural landscape, trees and flowers, outdoor scene"
            StoryCategory.DAILY_LIFE -> "everyday life scene, familiar activities, cozy and friendly"
        }

        val protagonistText = if (protagonist.isNotBlank()) {
            "featuring $protagonist as the main character, "
        } else {
            ""
        }

        return "A beautiful cover illustration for a children's story titled '$storyTitle', " +
                "$protagonistText$categoryStyle, $baseStyle, " +
                "book cover design, centered composition, no text"
    }

    /**
     * 構建段落配圖 Prompt
     */
    private fun buildSegmentImagePrompt(
        segmentContent: String,
        characterRole: String,
        storyTheme: String
    ): String {
        // 簡化段落內容，提取關鍵信息
        val simplifiedContent = segmentContent.take(100).trim()

        return "Children's book illustration showing: $simplifiedContent, " +
                "cute cartoon style, colorful and friendly, " +
                "suitable for young children, whimsical art, " +
                "soft colors, high quality digital art, no text"
    }

    /**
     * 調用圖片生成 API
     */
    private suspend fun callImageGenerationAPI(prompt: String): ByteArray? {
        return try {
            val jsonBody = JSONObject().apply {
                put("data", JSONArray().apply {
                    put(prompt)  // prompt
                    put("1024x1024 ( 1:1 )")  // resolution - 正方形適合封面
                    put((Math.random() * 1000000).toInt())  // random seed
                    put(8)  // steps - 快速生成
                    put(3.0)  // shift
                    put(true)  // random_seed
                    put(JSONArray())  // output_gallery
                })
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/api/generate")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody)

                // 解析返回的圖片路徑
                // 響應格式: {"data": [[{"image": "/path/to/image.png", "caption": null}], "42", 42]}
                val dataArray = jsonResponse.optJSONArray("data")
                if (dataArray != null && dataArray.length() > 0) {
                    val galleryArray = dataArray.optJSONArray(0)
                    if (galleryArray != null && galleryArray.length() > 0) {
                        val imageObject = galleryArray.getJSONObject(0)
                        val imagePath = imageObject.optString("image")

                        if (imagePath.isNotBlank()) {
                            // 下載圖片
                            return downloadImage("$baseUrl/file=$imagePath")
                        }
                    }
                }
            }

            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 下載圖片
     */
    private suspend fun downloadImage(imageUrl: String): ByteArray? {
        return try {
            val request = Request.Builder()
                .url(imageUrl)
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                response.body?.bytes()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 將圖片保存到本地
     */
    private fun saveImageToFile(imageData: ByteArray, filename: String): String? {
        return try {
            // 保存到應用私有目錄
            val imagesDir = File(context.filesDir, "story_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val imageFile = File(imagesDir, filename)

            // 將 ByteArray 解碼為 Bitmap
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)

            // 壓縮並保存
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }

            imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 檢查服務是否可用
     */
    suspend fun isServiceAvailable(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val request = Request.Builder()
                .url(baseUrl)
                .get()
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
