package com.example.kidsstory.data.remote

import com.example.kidsstory.BuildConfig
import com.example.kidsstory.domain.model.CharacterRole
import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.model.StoryCategory
import com.example.kidsstory.domain.model.StorySegment
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 使用 Google Gemini API 生成兒童故事
 */
@Singleton
class GeminiStoryGenerator @Inject constructor() {

    private val model: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash-exp",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.9f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 2048
            },
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
            )
        )
    }

    /**
     * 生成故事
     * @param theme 主題（例如：冒險、友誼、家庭）
     * @param protagonist 主角（例如：小兔子、小熊、小女孩）
     * @param educationalGoal 教育意義（例如：勇敢、分享、誠實）
     * @param language 語言（"zh" 或 "en"）
     */
    suspend fun generateStory(
        theme: String,
        protagonist: String,
        educationalGoal: String,
        language: String = "zh"
    ): Story? = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPrompt(theme, protagonist, educationalGoal, language)
            val response = model.generateContent(prompt)
            val text = response.text ?: return@withContext null

            parseStoryFromResponse(text, theme, language)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun buildPrompt(
        theme: String,
        protagonist: String,
        educationalGoal: String,
        language: String
    ): String {
        return if (language == "zh") {
            """
你是一位專業的兒童故事作家，請為 3-6 歲的學齡前兒童創作一個故事。

**要求**：
1. 主題：$theme
2. 主角：$protagonist
3. 教育意義：教導孩子關於「$educationalGoal」的重要性
4. 長度：8-10 個段落
5. 語言：繁體中文（台灣）
6. 每個段落 1-3 句話，簡單易懂
7. 使用積極正面的語言
8. 避免暴力、恐怖或不適合兒童的內容

**故事結構**：
- 開頭：介紹主角和環境
- 發展：遇到挑戰或問題
- 高潮：主角運用「$educationalGoal」解決問題
- 結尾：快樂的結局和教育意義總結

**輸出格式**（請嚴格遵守 JSON 格式）：
```json
{
  "title": "故事標題",
  "titleEn": "Story Title in English",
  "category": "FRIENDSHIP/ADVENTURE/FAMILY/EDUCATIONAL/MORAL",
  "ageRange": "3-6",
  "segments": [
    {
      "sequenceNumber": 1,
      "contentZh": "第一段中文內容",
      "contentEn": "First paragraph in English",
      "characterRole": "NARRATOR",
      "duration": 5
    }
  ]
}
```

**角色類型說明**：
- NARRATOR：旁白
- PROTAGONIST：主角
- SUPPORTING：配角
- ANTAGONIST：反派（僅在必要時使用，且要溫和）

請只輸出 JSON，不要有其他文字。
            """.trimIndent()
        } else {
            """
You are a professional children's story writer. Please create a story for preschool children aged 3-6.

**Requirements**:
1. Theme: $theme
2. Protagonist: $protagonist
3. Educational Goal: Teach children about the importance of "$educationalGoal"
4. Length: 8-10 segments
5. Language: English
6. Each segment should be 1-3 sentences, simple and easy to understand
7. Use positive and encouraging language
8. Avoid violence, horror, or inappropriate content

**Story Structure**:
- Beginning: Introduce the protagonist and setting
- Development: Face challenges or problems
- Climax: Protagonist uses "$educationalGoal" to solve the problem
- Ending: Happy conclusion with educational message summary

**Output Format** (Please strictly follow JSON format):
```json
{
  "title": "Story Title in Chinese",
  "titleEn": "Story Title",
  "category": "FRIENDSHIP/ADVENTURE/FAMILY/EDUCATIONAL/MORAL",
  "ageRange": "3-6",
  "segments": [
    {
      "sequenceNumber": 1,
      "contentZh": "Content in Traditional Chinese",
      "contentEn": "First paragraph content",
      "characterRole": "NARRATOR",
      "duration": 5
    }
  ]
}
```

**Character Roles**:
- NARRATOR: Narrator
- PROTAGONIST: Main character
- SUPPORTING: Supporting character
- ANTAGONIST: Antagonist (use sparingly and gently)

Please output only JSON, no other text.
            """.trimIndent()
        }
    }

    private fun parseStoryFromResponse(responseText: String, theme: String, language: String): Story? {
        try {
            // 提取 JSON（可能包含在 ```json ``` 中）
            val jsonText = extractJSON(responseText)
            val json = JSONObject(jsonText)

            val title = json.optString("title", "AI 生成故事")
            val titleEn = json.optString("titleEn", "AI Generated Story")
            val categoryStr = json.optString("category", "EDUCATIONAL")
            val ageRange = json.optString("ageRange", "3-6")
            val segmentsArray = json.optJSONArray("segments") ?: JSONArray()

            val segments = mutableListOf<StorySegment>()
            for (i in 0 until segmentsArray.length()) {
                val segJson = segmentsArray.getJSONObject(i)
                segments.add(
                    StorySegment(
                        sequenceNumber = segJson.optInt("sequenceNumber", i + 1),
                        contentZh = segJson.optString("contentZh", ""),
                        contentEn = segJson.optString("contentEn", ""),
                        characterRole = CharacterRole.fromString(
                            segJson.optString("characterRole", "NARRATOR")
                        ),
                        duration = segJson.optInt("duration", 5),
                        audioPathZh = null,
                        audioPathEn = null
                    )
                )
            }

            if (segments.isEmpty()) {
                return null
            }

            return Story(
                id = "ai_${System.currentTimeMillis()}",
                title = title,
                titleEn = titleEn,
                category = StoryCategory.fromString(categoryStr),
                ageRange = ageRange,
                coverImage = "",
                segments = segments,
                isPreset = false,
                isDownloaded = false,
                createdAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun extractJSON(text: String): String {
        // 移除 markdown 代碼塊標記
        val codeBlockPattern = "```(?:json)?\\s*([\\s\\S]*?)```".toRegex()
        val match = codeBlockPattern.find(text)
        if (match != null) {
            return match.groupValues[1].trim()
        }

        // 尋找 JSON 開始和結束位置
        val startIndex = text.indexOf("{")
        val endIndex = text.lastIndexOf("}")

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return text.substring(startIndex, endIndex + 1)
        }

        return text.trim()
    }
}
