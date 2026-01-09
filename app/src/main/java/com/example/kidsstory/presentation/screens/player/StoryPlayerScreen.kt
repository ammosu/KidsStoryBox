package com.example.kidsstory.presentation.screens.player

import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kidsstory.domain.model.Language
import java.io.File
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryPlayerScreen(
    storyId: String,
    onNavigateBack: () -> Unit,
    viewModel: StoryPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error
                                ?: if (isEnglish) "Something went wrong" else "發生錯誤",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isEnglish) "Tap to go back" else "點擊返回",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable { onNavigateBack() }
                        )
                    }
                }
            }

            uiState.story == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isEnglish) "Story not found\nTap to go back" else "找不到故事內容\n點擊返回")
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

                val segmentImage = segment?.image

                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = segmentImage,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "segment_image"
                    ) { imagePath ->
                        val context = LocalContext.current
                        if (imagePath != null) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // 背景漸層作為後備
                                GradientBackground()
                                // 圖片覆蓋在上層
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(imagePath))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = if (isEnglish) {
                                        "Story illustration"
                                    } else {
                                        "故事插圖"
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            GradientBackground()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    )

                    TopAppBar(
                        title = {
                            Text(
                                text = if (isEnglish) story?.titleEn ?: "" else story?.title ?: "",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.stopPlayback()
                                onNavigateBack()
                            }) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isEnglish) "Close" else "關閉"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = if (isEnglish) "Settings" else "設定"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 48.dp
                            )
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            if (uiState.segmentCount > 0) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isEnglish) {
                                                "Segment ${uiState.currentSegmentIndex + 1}"
                                            } else {
                                                "第 ${uiState.currentSegmentIndex + 1} 段"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                        Text(
                                            text = "${uiState.currentSegmentIndex + 1} / ${uiState.segmentCount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }

                                    Slider(
                                        value = uiState.currentSegmentIndex.toFloat(),
                                        onValueChange = { newValue ->
                                            viewModel.seekToSegment(newValue.toInt())
                                        },
                                        valueRange = 0f..(max(0, uiState.segmentCount - 1).toFloat()),
                                        steps = max(0, uiState.segmentCount - 2),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(20.dp),
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = Color.White,
                                            activeTrackColor = Color.White.copy(alpha = 0.8f),
                                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.95f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = segmentText.ifBlank {
                                            if (isEnglish) "No content yet" else "尚無內容"
                                        },
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 32.sp
                                        ),
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF1A1A1A)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 上一段按鈕
                                        PlaybackButton(
                                            icon = Icons.Default.SkipPrevious,
                                            contentDescription = if (isEnglish) "Previous" else "上一段",
                                            onClick = { viewModel.previousSegment() },
                                            size = 56.dp,
                                            iconSize = 32.dp,
                                            enabled = uiState.currentSegmentIndex > 0
                                        )

                                        Spacer(modifier = Modifier.width(16.dp))

                                        // 播放/暫停按鈕
                                        PlaybackButton(
                                            icon = if (uiState.isPlaying) {
                                                Icons.Default.Pause
                                            } else {
                                                Icons.Default.PlayArrow
                                            },
                                            contentDescription = if (uiState.isPlaying) {
                                                if (isEnglish) "Pause" else "暫停"
                                            } else {
                                                if (isEnglish) "Play" else "播放"
                                            },
                                            onClick = { viewModel.togglePlayPause() },
                                            size = 64.dp,
                                            iconSize = 36.dp,
                                            enabled = true
                                        )

                                        Spacer(modifier = Modifier.width(16.dp))

                                        // 下一段按鈕
                                        PlaybackButton(
                                            icon = Icons.Default.SkipNext,
                                            contentDescription = if (isEnglish) "Next" else "下一段",
                                            onClick = { viewModel.nextSegment() },
                                            size = 56.dp,
                                            iconSize = 32.dp,
                                            enabled = uiState.currentSegmentIndex < uiState.segmentCount - 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(
                                bottom = WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding() + 80.dp
                            )
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (isEnglish) {
                                    "${uiState.currentSegmentIndex + 1} / ${uiState.segmentCount}"
                                } else {
                                    "${uiState.currentSegmentIndex + 1} / ${uiState.segmentCount}"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (isEnglish) "Segments" else "段落",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier
                                    .clickable { showSegments = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (showSettings) {
                    SettingsBottomSheet(
                        isEnglish = isEnglish,
                        speechRate = uiState.speechRate,
                        speechPitch = uiState.speechPitch,
                        onSpeechRateChange = { viewModel.updateSpeechRate(it) },
                        onSpeechPitchChange = { viewModel.updateSpeechPitch(it) },
                        onDismiss = { showSettings = false }
                    )
                }

                if (showSegments && story != null) {
                    ModalBottomSheet(
                        onDismissRequest = { showSegments = false }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(
                                    bottom = WindowInsets.navigationBars.asPaddingValues()
                                        .calculateBottomPadding()
                                )
                        ) {
                            Text(
                                text = if (isEnglish) "Segments" else "段落列表",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp),
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
fun GradientBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2),
                        Color(0xFFf093fb)
                    )
                )
            )
    )
}

@Composable
fun PlaybackButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 64.dp,
    iconSize: androidx.compose.ui.unit.Dp = 36.dp,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    isEnglish: Boolean,
    speechRate: Float,
    speechPitch: Float,
    onSpeechRateChange: (Float) -> Unit,
    onSpeechPitchChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                )
        ) {
            Text(
                text = if (isEnglish) "Playback Settings" else "播放設定",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(24.dp))

            val speedLabel = if (isEnglish) "Speed" else "語速"
            val pitchLabel = if (isEnglish) "Pitch" else "音調"

            Text(
                text = "$speedLabel: ${String.format("%.2fx", speechRate)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = speechRate,
                onValueChange = onSpeechRateChange,
                valueRange = 0.5f..1.5f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$pitchLabel: ${String.format("%.2fx", speechPitch)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = speechPitch,
                onValueChange = onSpeechPitchChange,
                valueRange = 0.5f..1.5f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
