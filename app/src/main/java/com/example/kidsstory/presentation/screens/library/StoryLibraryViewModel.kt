package com.example.kidsstory.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.model.StoryCategory
import com.example.kidsstory.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 故事列表畫面的 ViewModel
 */
@HiltViewModel
class StoryLibraryViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryLibraryUiState())
    val uiState: StateFlow<StoryLibraryUiState> = _uiState.asStateFlow()

    init {
        loadStories()
    }

    private fun loadStories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                storyRepository.getAllStories().collect { stories ->
                    _uiState.update {
                        it.copy(
                            stories = stories,
                            filteredStories = filterStories(stories, it.selectedCategory),
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "載入故事失敗"
                    )
                }
            }
        }
    }

    fun selectCategory(category: StoryCategory?) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                filteredStories = filterStories(it.stories, category)
            )
        }
    }

    private fun filterStories(stories: List<Story>, category: StoryCategory?): List<Story> {
        return if (category == null) {
            stories
        } else {
            stories.filter { it.category == category }
        }
    }
}

/**
 * 故事列表 UI 狀態
 */
data class StoryLibraryUiState(
    val stories: List<Story> = emptyList(),
    val filteredStories: List<Story> = emptyList(),
    val selectedCategory: StoryCategory? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
