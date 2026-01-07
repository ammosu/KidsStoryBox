package com.example.kidsstory.presentation.screens.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kidsstory.domain.model.Language
import kotlin.math.max

/**
 * 故事播放器畫面
 * 播放多角色語音並顯示字幕和圖片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryPlayerScreen(
    storyId: String,
    onNavigateBack: () -> Unit,
    viewModel: StoryPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSegments by remember { mutableStateOf(false) }
    val isEnglish = uiState.language == Language.ENGLISH

    LaunchedEffect(storyId) {
        viewModel.loadStory(storyId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPlayback()
        }
    }

    Scaffold(
        topBar = {
            val appBarTitle = uiState.story?.let { story ->
                if (uiState.language == Language.ENGLISH) {
                    story.titleEn.ifBlank { story.title }
                } else {
                    story.title
                }
            } ?: if (uiState.language == Language.ENGLISH) {
                "Story Player"
            } else {
                "故事播放"
            }
            TopAppBar(
                title = { Text(appBarTitle) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopPlayback()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error
                            ?: if (isEnglish) "Something went wrong" else "發生錯誤",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            uiState.story == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isEnglish) "Story not found" else "找不到故事內容")
                }
            }

            else -> {
                val story = uiState.story
                val segment = story?.segments?.getOrNull(uiState.currentSegmentIndex)
                val segmentText = when (uiState.language) {
                    Language.CHINESE -> segment?.contentZh ?: ""
                    Language.ENGLISH -> segment?.contentEn ?: ""
                }.ifBlank {
                    segment?.contentZh?.ifBlank { segment?.contentEn ?: "" } ?: ""
                }
                val emptyContentLabel = if (isEnglish) "No content yet" else "尚無內容"
                val minutes = max(1, (story?.duration ?: 0) / 60)
                val categoryLabel = if (isEnglish) {
                    story?.category?.displayNameEn ?: ""
                } else {
                    story?.category?.displayNameZh ?: ""
                }
                val ageLabel = if (isEnglish) {
                    "${story?.ageRange ?: ""} yrs"
                } else {
                    "${story?.ageRange ?: ""} 歲"
                }
                val durationLabel = if (isEnglish) {
                    "$minutes min"
                } else {
                    "約 $minutes 分鐘"
                }
                val segmentLabel = if (isEnglish) {
                    "Segment ${uiState.currentSegmentIndex + 1}"
                } else {
                    "第 ${uiState.currentSegmentIndex + 1} 段"
                }
                val segmentListLabel = if (isEnglish) "Segments" else "段落列表"
                val settingsLabel = if (isEnglish) "Playback settings" else "播放設定"
                val speedLabel = if (isEnglish) "Speed" else "語速"
                val pitchLabel = if (isEnglish) "Pitch" else "音調"

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (categoryLabel.isNotBlank()) {
                            StoryMetaPill(text = categoryLabel)
                        }
                        if (story?.ageRange?.isNotBlank() == true) {
                            StoryMetaPill(text = ageLabel)
                        }
                        StoryMetaPill(text = durationLabel)
                    }

                    Text(
                        text = segmentLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = segmentText.ifBlank { emptyContentLabel },
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (uiState.segmentCount > 0) {
                        val progress = (uiState.currentSegmentIndex + 1).toFloat() /
                            uiState.segmentCount.toFloat()
                        LinearProgressIndicator(
                            progress = progress.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${uiState.currentSegmentIndex + 1} / ${uiState.segmentCount}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = { showSegments = true }) {
                                Text(segmentListLabel)
                            }
                        }
                    }

                    Text(
                        text = settingsLabel,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$speedLabel ${String.format("%.2fx", uiState.speechRate)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Slider(
                                value = uiState.speechRate,
                                onValueChange = { viewModel.updateSpeechRate(it) },
                                valueRange = 0.5f..1.5f
                            )
                            Text(
                                text = "$pitchLabel ${String.format("%.2fx", uiState.speechPitch)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Slider(
                                value = uiState.speechPitch,
                                onValueChange = { viewModel.updateSpeechPitch(it) },
                                valueRange = 0.5f..1.5f
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.previousSegment() },
                            enabled = uiState.currentSegmentIndex > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "上一段"
                            )
                        }

                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            enabled = uiState.segmentCount > 0,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },
                                contentDescription = if (uiState.isPlaying) "暫停" else "播放",
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.nextSegment() },
                            enabled = uiState.currentSegmentIndex < uiState.segmentCount - 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "下一段"
                            )
                        }
                    }

                    if (uiState.ttsError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.ttsError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (showSegments && story != null) {
                    ModalBottomSheet(
                        onDismissRequest = { showSegments = false }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = segmentListLabel,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 360.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(story.segments) { index, item ->
                                    val isCurrent = index == uiState.currentSegmentIndex
                                    val preview = when (uiState.language) {
                                        Language.CHINESE -> item.contentZh
                                        Language.ENGLISH -> item.contentEn
                                    }.ifBlank {
                                        item.contentZh.ifBlank { item.contentEn }
                                    }
                                    val displayText = preview.ifBlank { "第 ${index + 1} 段" }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.seekToSegment(index)
                                                showSegments = false
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isCurrent) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = if (isEnglish) {
                                                    "Segment ${index + 1}"
                                                } else {
                                                    "第 ${index + 1} 段"
                                                },
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = displayText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
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
