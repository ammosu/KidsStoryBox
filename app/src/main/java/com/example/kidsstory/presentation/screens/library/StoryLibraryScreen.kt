package com.example.kidsstory.presentation.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.kidsstory.domain.model.Language
import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.model.StoryCategory
import java.io.File
import kotlin.math.max

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
                title = {
                    Text(if (uiState.language == Language.ENGLISH) "Story Library" else "故事寶庫")
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAIGenerationClick
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (uiState.language == Language.ENGLISH) "Create story" else "創造新故事")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LibraryHeader(
                storyCount = uiState.stories.size,
                language = uiState.language
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (uiState.language == Language.ENGLISH) "Language" else "語言",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LanguageFilterRow(
                selectedLanguage = uiState.language,
                onLanguageSelect = { viewModel.selectLanguage(it) }
            )

            Text(
                text = if (uiState.language == Language.ENGLISH) "Categories" else "分類",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp)
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
                            text = uiState.error
                                ?: if (uiState.language == Language.ENGLISH) {
                                    "Something went wrong"
                                } else {
                                    "發生錯誤"
                                },
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                uiState.filteredStories.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (uiState.language == Language.ENGLISH) {
                                "No stories yet"
                            } else {
                                "還沒有故事"
                            }
                        )
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
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            val allLabel = if (language == Language.ENGLISH) "All" else "全部"
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelect(null) },
                label = { Text(allLabel) }
            )
        }
        items(StoryCategory.values()) { category ->
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
fun LibraryHeader(
    storyCount: Int,
    language: Language
) {
    val title = if (language == Language.ENGLISH) "Story Library" else "故事寶庫"
    val subtitle = if (language == Language.ENGLISH) {
        "Pick a tale and start the adventure"
    } else {
        "挑一則故事開始冒險"
    }
    val countLabel = if (language == Language.ENGLISH) {
        "$storyCount stories available"
    } else {
        "共 $storyCount 則故事"
    }
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = countLabel,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun StoryCard(
    story: Story,
    language: Language,
    onClick: () -> Unit
) {
    val minutes = max(1, story.duration / 60)
    val title = when (language) {
        Language.CHINESE -> story.title.ifBlank { story.titleEn }
        Language.ENGLISH -> story.titleEn.ifBlank { story.title }
    }
    val categoryLabel = if (language == Language.ENGLISH) {
        story.category.displayNameEn
    } else {
        story.category.displayNameZh
    }
    val durationLabel = if (language == Language.ENGLISH) {
        "$minutes min"
    } else {
        "約 $minutes 分鐘"
    }
    val ageLabel = if (language == Language.ENGLISH) {
        "${story.ageRange} yrs"
    } else {
        "${story.ageRange} 歲"
    }
    val badgeLabel = if (story.isPreset) {
        if (language == Language.ENGLISH) "Preset" else "預設"
    } else {
        "AI"
    }
    val headerGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // 封面圖片或漸變背景
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // 如果有封面圖片，顯示圖片；否則顯示漸變背景
                if (story.coverImage.isNotBlank() && File(story.coverImage).exists()) {
                    AsyncImage(
                        model = File(story.coverImage),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // 在圖片上疊加半透明遮罩，讓文字更清晰
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                } else {
                    // 沒有圖片時使用漸變背景
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(headerGradient)
                    )
                }

                // 標籤和分類文字覆蓋在上層
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = categoryLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,  // 確保在圖片上也清晰可見
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StoryMetaPill(text = ageLabel)
                    StoryMetaPill(text = durationLabel)
                }
            }
        }
    }
}

@Composable
fun StoryMetaPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
