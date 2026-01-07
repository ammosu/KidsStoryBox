package com.example.kidsstory.domain.usecases

import com.example.kidsstory.data.remote.GeminiStoryGenerator
import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.repository.StoryRepository
import javax.inject.Inject

/**
 * 使用 AI 生成故事並儲存到資料庫
 */
class GenerateStoryUseCase @Inject constructor(
    private val geminiGenerator: GeminiStoryGenerator,
    private val repository: StoryRepository
) {
    /**
     * @param theme 主題（例如：冒險、友誼、家庭）
     * @param protagonist 主角（例如：小兔子、小熊、小女孩）
     * @param educationalGoal 教育意義（例如：勇敢、分享、誠實）
     * @param language 語言（"zh" 或 "en"）
     * @return 生成並儲存的故事，失敗返回 null
     */
    suspend operator fun invoke(
        theme: String,
        protagonist: String,
        educationalGoal: String,
        language: String = "zh"
    ): Result<Story> {
        return try {
            // 1. 使用 Gemini 生成故事
            val story = geminiGenerator.generateStory(
                theme = theme,
                protagonist = protagonist,
                educationalGoal = educationalGoal,
                language = language
            ) ?: return Result.failure(Exception("AI 生成故事失敗"))

            // 2. 儲存到資料庫
            repository.saveStory(story)

            Result.success(story)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
