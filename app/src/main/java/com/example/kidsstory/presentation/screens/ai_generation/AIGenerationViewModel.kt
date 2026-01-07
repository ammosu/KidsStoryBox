package com.example.kidsstory.presentation.screens.ai_generation

import androidx.lifecycle.ViewModel
import com.example.kidsstory.domain.model.Language
import com.example.kidsstory.domain.model.StoryCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * AI 故事生成畫面 ViewModel（目前為 UI 狀態管理）
 */
@HiltViewModel
class AIGenerationViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AIGenerationUiState())
    val uiState: StateFlow<AIGenerationUiState> = _uiState.asStateFlow()

    fun updateTopic(topic: String) {
        _uiState.update { it.copy(topic = topic) }
    }

    fun updateCharacters(characters: String) {
        _uiState.update { it.copy(characters = characters) }
    }

    fun updateSetting(setting: String) {
        _uiState.update { it.copy(setting = setting) }
    }

    fun selectCategory(category: StoryCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectLanguage(language: Language) {
        _uiState.update { it.copy(language = language) }
    }

    fun generateStory() {
        _uiState.update {
            it.copy(
                isGenerating = false,
                error = "AI 生成尚未整合，請稍後再試"
            )
        }
    }
}

data class AIGenerationUiState(
    val topic: String = "",
    val characters: String = "",
    val setting: String = "",
    val selectedCategory: StoryCategory? = null,
    val language: Language = Language.CHINESE,
    val isGenerating: Boolean = false,
    val error: String? = null
)
