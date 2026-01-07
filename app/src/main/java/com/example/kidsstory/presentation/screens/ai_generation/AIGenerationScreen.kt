package com.example.kidsstory.presentation.screens.ai_generation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kidsstory.domain.model.Language
import com.example.kidsstory.domain.model.StoryCategory

/**
 * AI故事生成畫面
 * 讓用戶選擇主題、角色等選項，然後生成新故事
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIGenerationScreen(
    onNavigateBack: () -> Unit,
    onStoryGenerated: (String) -> Unit,
    viewModel: AIGenerationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("創造你的故事") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "告訴我們你想要的故事設定",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = uiState.topic,
                onValueChange = viewModel::updateTopic,
                label = { Text("主題或關鍵字") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.characters,
                onValueChange = viewModel::updateCharacters,
                label = { Text("角色（可選）") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.setting,
                onValueChange = viewModel::updateSetting,
                label = { Text("場景（可選）") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "故事分類",
                style = MaterialTheme.typography.titleSmall
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                item {
                    val label = if (uiState.language == Language.ENGLISH) "All" else "全部"
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text(label) }
                    )
                }
                items(StoryCategory.values()) { category ->
                    val label = if (uiState.language == Language.ENGLISH) {
                        category.displayNameEn
                    } else {
                        category.displayNameZh
                    }
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(label) }
                    )
                }
            }

            Text(
                text = "語言",
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.language == Language.CHINESE,
                    onClick = { viewModel.selectLanguage(Language.CHINESE) },
                    label = { Text("中文") }
                )
                FilterChip(
                    selected = uiState.language == Language.ENGLISH,
                    onClick = { viewModel.selectLanguage(Language.ENGLISH) },
                    label = { Text("English") }
                )
            }

            Button(
                onClick = {
                    viewModel.generateStory()
                },
                enabled = !uiState.isGenerating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("生成故事")
            }

            if (uiState.isGenerating) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "提示：AI 生成服務尚未串接，會在後續版本加入。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
