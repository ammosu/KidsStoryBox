package com.example.kidsstory.presentation.screens.ai_generation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidsstory.domain.model.Language
import com.example.kidsstory.domain.model.StoryCategory
import com.example.kidsstory.domain.usecases.GenerateStoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * AI 故事生成畫面 ViewModel
 */
@HiltViewModel
class AIGenerationViewModel @Inject constructor(
    private val generateStoryUseCase: GenerateStoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIGenerationUiState())
    val uiState: StateFlow<AIGenerationUiState> = _uiState.asStateFlow()

    fun updateTopic(topic: String) {
        _uiState.update { it.copy(topic = topic, error = null) }
    }

    fun updateCharacters(characters: String) {
        _uiState.update { it.copy(characters = characters, error = null) }
    }

    fun updateSetting(setting: String) {
        _uiState.update { it.copy(setting = setting, error = null) }
    }

    fun selectCategory(category: StoryCategory?) {
        _uiState.update { it.copy(selectedCategory = category, error = null) }
    }

    fun selectLanguage(language: Language) {
        _uiState.update { it.copy(language = language, error = null) }
    }

    fun generateStory() {
        val state = _uiState.value

        if (state.topic.isBlank()) {
            _uiState.update {
                it.copy(
                    error = if (it.language == Language.ENGLISH) {
                        "Please enter a topic"
                    } else {
                        "請輸入主題"
                    }
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    error = null,
                    generatedStoryId = null,
                    generatingStep = if (it.language == Language.ENGLISH) {
                        "Creating story..."
                    } else {
                        "正在創作故事..."
                    }
                )
            }

            try {
                // 構建 AI 提示詞
                val theme = buildTheme(state)
                val protagonist = state.characters.ifBlank {
                    if (state.language == Language.ENGLISH) "a brave child" else "一個勇敢的孩子"
                }
                val educationalGoal = state.selectedCategory?.let {
                    if (state.language == Language.ENGLISH) {
                        it.displayNameEn
                    } else {
                        it.displayNameZh
                    }
                } ?: if (state.language == Language.ENGLISH) "courage and kindness" else "勇敢與善良"

                val languageCode = if (state.language == Language.ENGLISH) "en" else "zh"

                // 更新進度：生成圖片
                _uiState.update {
                    it.copy(
                        generatingStep = if (it.language == Language.ENGLISH) {
                            "Generating cover illustration..."
                        } else {
                            "正在繪製封面插圖..."
                        }
                    )
                }

                // 調用 AI 生成（包含圖片生成）
                val result = generateStoryUseCase(
                    theme = theme,
                    protagonist = protagonist,
                    educationalGoal = educationalGoal,
                    language = languageCode,
                    generateCoverImage = true
                )

                result.fold(
                    onSuccess = { story ->
                        _uiState.update {
                            it.copy(
                                isGenerating = false,
                                generatedStoryId = story.id,
                                generatingStep = null,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isGenerating = false,
                                generatingStep = null,
                                error = if (it.language == Language.ENGLISH) {
                                    "Failed to generate story: ${error.message}"
                                } else {
                                    "故事生成失敗：${error.message}"
                                }
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generatingStep = null,
                        error = if (it.language == Language.ENGLISH) {
                            "Error: ${e.message}"
                        } else {
                            "發生錯誤：${e.message}"
                        }
                    )
                }
            }
        }
    }

    private fun buildTheme(state: AIGenerationUiState): String {
        val parts = mutableListOf<String>()
        parts.add(state.topic)

        if (state.setting.isNotBlank()) {
            parts.add(state.setting)
        }

        return parts.joinToString(" ")
    }
}

data class AIGenerationUiState(
    val topic: String = "",
    val characters: String = "",
    val setting: String = "",
    val selectedCategory: StoryCategory? = null,
    val language: Language = Language.CHINESE,
    val isGenerating: Boolean = false,
    val generatingStep: String? = null,  // 當前生成步驟的說明
    val generatedStoryId: String? = null,
    val error: String? = null
)
