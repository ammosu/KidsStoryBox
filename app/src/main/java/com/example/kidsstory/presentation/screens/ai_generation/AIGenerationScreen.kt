package com.example.kidsstory.presentation.screens.ai_generation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * AI故事生成畫面
 * 讓用戶選擇主題、角色等選項，然後生成新故事
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIGenerationScreen(
    onNavigateBack: () -> Unit,
    onStoryGenerated: (String) -> Unit
) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("AI生成介面（開發中）")
        }
    }
}
