package com.example.kidsstory.presentation.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kidsstory.domain.model.Language
import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.model.StoryCategory

/**
 * 故事列表畫面
 * 顯示所有可用的故事（預設 + AI生成）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryLibraryScreen(
    onStoryClick: (String) -> Unit,
    onAIGenerationClick: () -> Unit,
    viewModel: StoryLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("故事寶庫") }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAIGenerationClick
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("創造新故事")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LanguageFilterRow(
                selectedLanguage = uiState.language,
                onLanguageSelect = { viewModel.selectLanguage(it) }
            )

            // 分類篩選
            CategoryFilterRow(
                language = uiState.language,
                selectedCategory = uiState.selectedCategory,
                onCategorySelect = { viewModel.selectCategory(it) }
            )

            // 故事網格
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "發生錯誤",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                uiState.filteredStories.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("還沒有故事")
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.filteredStories) { story ->
                            StoryCard(
                                story = story,
                                language = uiState.language,
                                onClick = { onStoryClick(story.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryFilterRow(
    language: Language,
    selectedCategory: StoryCategory?,
    onCategorySelect: (StoryCategory?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 全部按鈕
        val allLabel = if (language == Language.ENGLISH) "All" else "全部"
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelect(null) },
            label = { Text(allLabel) }
        )

        // 各分類按鈕
        StoryCategory.values().forEach { category ->
            val label = if (language == Language.ENGLISH) {
                category.displayNameEn
            } else {
                category.displayNameZh
            }
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelect(category) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun LanguageFilterRow(
    selectedLanguage: Language,
    onLanguageSelect: (Language) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedLanguage == Language.CHINESE,
            onClick = { onLanguageSelect(Language.CHINESE) },
            label = { Text("中文") }
        )
        FilterChip(
            selected = selectedLanguage == Language.ENGLISH,
            onClick = { onLanguageSelect(Language.ENGLISH) },
            label = { Text("English") }
        )
    }
}

@Composable
fun StoryCard(
    story: Story,
    language: Language,
    onClick: () -> Unit
) {
    val title = when (language) {
        Language.CHINESE -> story.title.ifBlank { story.titleEn }
        Language.ENGLISH -> story.titleEn.ifBlank { story.title }
    }
    val categoryLabel = if (language == Language.ENGLISH) {
        story.category.displayNameEn
    } else {
        story.category.displayNameZh
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // 封面圖片佔位符
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // 暫時使用文字代替圖片
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = categoryLabel,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 故事資訊
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${story.duration / 60} 分鐘",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
