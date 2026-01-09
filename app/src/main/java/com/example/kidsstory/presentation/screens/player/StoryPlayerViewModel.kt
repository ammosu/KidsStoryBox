package com.example.kidsstory.presentation.screens.player

import com.example.kidsstory.data.tts.AndroidTTSService
import com.example.kidsstory.data.tts.TTSPlaybackListener
import com.example.kidsstory.domain.model.Language
import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.usecases.GetStoryByIdUseCase
import com.example.kidsstory.domain.usecases.UpdateLastPlayedAtUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@HiltViewModel
class StoryPlayerViewModel @Inject constructor(
    private val ttsService: AndroidTTSService,
    private val getStoryByIdUseCase: GetStoryByIdUseCase,
    private val updateLastPlayedAtUseCase: UpdateLastPlayedAtUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryPlayerUiState())
    val uiState: StateFlow<StoryPlayerUiState> = _uiState.asStateFlow()

    init {
        // 設定 TTS 播放事件監聽器
        ttsService.setPlaybackListener(object : TTSPlaybackListener {
            override fun onSegmentComplete(utteranceId: String) {
                handleSegmentComplete()
            }

            override fun onPlaybackError(utteranceId: String?, error: String) {
                viewModelScope.launch {
                    _uiState.update { it.copy(ttsError = error) }
                }
            }
        })
    }

    fun loadStory(storyId: String) {
        viewModelScope.launch {
            stopPlaybackInternal()
            _uiState.update { it.copy(isLoading = true, error = null) }

            val story = getStoryByIdUseCase(storyId)
            if (story == null) {
                _uiState.update {
                    it.copy(isLoading = false, error = "找不到故事")
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    story = story,
                    currentSegmentIndex = 0,
                    error = null
                )
            }
        }
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            stopPlaybackInternal()
        } else {
            startPlayback()
        }
    }

    fun stopPlayback() {
        stopPlaybackInternal()
    }

    fun nextSegment() {
        moveToSegment(_uiState.value.currentSegmentIndex + 1, _uiState.value.isPlaying)
    }

    fun previousSegment() {
        moveToSegment(_uiState.value.currentSegmentIndex - 1, _uiState.value.isPlaying)
    }

    fun selectLanguage(language: Language) {
        _uiState.update { it.copy(language = language) }

        // 設定 TTS 語言
        val languageCode = when (language) {
            Language.CHINESE -> "zh"
            Language.ENGLISH -> "en"
        }

        val success = ttsService.setLanguage(languageCode)
        if (!success) {
            _uiState.update { it.copy(ttsError = "無法切換至該語言") }
        }

        // 如果正在播放，重新播放當前段落以應用語言變更
        if (_uiState.value.isPlaying) {
            playCurrentSegmentInternal()
        }
    }

    fun updateSpeechRate(rate: Float) {
        val clampedRate = rate.coerceIn(0.5f, 1.5f)
        _uiState.update { it.copy(speechRate = clampedRate) }
        ttsService.setSpeechRate(clampedRate)
    }

    fun updateSpeechPitch(pitch: Float) {
        val clampedPitch = pitch.coerceIn(0.5f, 1.5f)
        _uiState.update { it.copy(speechPitch = clampedPitch) }
        ttsService.setPitch(clampedPitch)
    }

    fun seekToSegment(index: Int) {
        moveToSegment(index, _uiState.value.isPlaying)
    }

    private fun startPlayback() {
        val story = _uiState.value.story ?: return
        viewModelScope.launch {
            updateLastPlayedAtUseCase(story.id, System.currentTimeMillis())
        }
        _uiState.update { it.copy(isPlaying = true, ttsError = null) }
        playCurrentSegmentInternal()
    }

    private fun stopPlaybackInternal() {
        ttsService.stop()
        _uiState.update { it.copy(isPlaying = false) }
    }

    private fun moveToSegment(index: Int, autoPlay: Boolean) {
        val story = _uiState.value.story ?: return
        if (index !in story.segments.indices) {
            if (autoPlay) {
                stopPlaybackInternal()
            }
            return
        }

        _uiState.update { it.copy(currentSegmentIndex = index) }
        if (autoPlay) {
            playCurrentSegmentInternal()
        }
    }

    private fun playCurrentSegmentInternal() {
        val state = _uiState.value
        val segment = state.story?.segments?.getOrNull(state.currentSegmentIndex) ?: return

        // 檢查 TTS 是否已初始化
        if (!ttsService.isReady()) {
            _uiState.update { it.copy(ttsError = "TTS 尚未初始化，請稍候...") }
            return
        }

        // 應用語速和音調設定
        ttsService.setSpeechRate(state.speechRate)
        ttsService.setPitch(state.speechPitch)

        // 根據選擇的語言獲取文字內容
        val text = when (state.language) {
            Language.CHINESE -> segment.contentZh.ifBlank { segment.contentEn }
            Language.ENGLISH -> segment.contentEn.ifBlank { segment.contentZh }
        }.trim()

        if (text.isBlank()) {
            android.util.Log.w("StoryPlayerViewModel", "Segment content is empty")
            return
        }

        // 開始播放
        val success = ttsService.speak(
            text = text,
            utteranceId = "segment_${state.currentSegmentIndex}"
        )

        if (!success) {
            _uiState.update { it.copy(ttsError = "無法開始播放") }
        }
    }

    /**
     * 處理段落播放完成
     * 由 TTS 服務的監聽器回調
     */
    private fun handleSegmentComplete() {
        val state = _uiState.value
        if (!state.isPlaying) {
            return
        }

        viewModelScope.launch {
            val story = state.story ?: return@launch
            val nextIndex = state.currentSegmentIndex + 1

            if (nextIndex >= story.segments.size) {
                // 已播放完所有段落，停止播放
                stopPlaybackInternal()
            } else {
                // 自動跳到下一段
                _uiState.update { it.copy(currentSegmentIndex = nextIndex) }
                playCurrentSegmentInternal()
            }
        }
    }

    override fun onCleared() {
        // 清理 TTS 監聽器
        ttsService.setPlaybackListener(null)
        super.onCleared()
    }
}

data class StoryPlayerUiState(
    val story: Story? = null,
    val currentSegmentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val ttsError: String? = null,
    val language: Language = Language.CHINESE,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f
) {
    val segmentCount: Int
        get() = story?.segments?.size ?: 0
}
