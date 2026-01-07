package com.example.kidsstory.presentation.screens.player

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.kidsstory.domain.model.Language
import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

/**
 * 故事播放器 ViewModel，整合 Android 系統 TTS
 */
@HiltViewModel
class StoryPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storyRepository: StoryRepository
) : ViewModel(), TextToSpeech.OnInitListener {

    private val _uiState = MutableStateFlow(StoryPlayerUiState())
    val uiState: StateFlow<StoryPlayerUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var ttsReady = false
    private var pendingSpeak = false

    init {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) = Unit

            override fun onDone(utteranceId: String) {
                handleUtteranceDone()
            }

            override fun onError(utteranceId: String) {
                viewModelScope.launch {
                    _uiState.update { it.copy(ttsError = "語音播放失敗") }
                }
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ttsReady = true
            setTtsLanguage(_uiState.value.language)
            if (pendingSpeak) {
                pendingSpeak = false
                playCurrentSegmentInternal()
            }
        } else {
            _uiState.update { it.copy(ttsError = "TTS 初始化失敗") }
        }
    }

    fun loadStory(storyId: String) {
        viewModelScope.launch {
            stopPlaybackInternal()
            _uiState.update { it.copy(isLoading = true, error = null) }

            val story = storyRepository.getStoryById(storyId)
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
        if (ttsReady) {
            setTtsLanguage(language)
        }
        if (_uiState.value.isPlaying) {
            playCurrentSegmentInternal()
        }
    }

    private fun startPlayback() {
        val story = _uiState.value.story ?: return
        viewModelScope.launch {
            storyRepository.updateLastPlayedAt(story.id, System.currentTimeMillis())
        }
        _uiState.update { it.copy(isPlaying = true, ttsError = null) }
        playCurrentSegmentInternal()
    }

    private fun stopPlaybackInternal() {
        pendingSpeak = false
        tts?.stop()
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

        if (!ttsReady) {
            pendingSpeak = true
            return
        }

        val text = when (state.language) {
            Language.CHINESE -> segment.contentZh.ifBlank { segment.contentEn }
            Language.ENGLISH -> segment.contentEn.ifBlank { segment.contentZh }
        }.trim()

        if (text.isBlank()) {
            return
        }

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "segment_${state.currentSegmentIndex}"
        )
    }

    private fun handleUtteranceDone() {
        val state = _uiState.value
        if (!state.isPlaying) {
            return
        }

        viewModelScope.launch {
            val story = state.story ?: return@launch
            val nextIndex = state.currentSegmentIndex + 1
            if (nextIndex >= story.segments.size) {
                stopPlaybackInternal()
            } else {
                _uiState.update { it.copy(currentSegmentIndex = nextIndex) }
                playCurrentSegmentInternal()
            }
        }
    }

    private fun setTtsLanguage(language: Language) {
        val locale = when (language) {
            Language.CHINESE -> Locale.TAIWAN
            Language.ENGLISH -> Locale.US
        }

        val result = tts?.setLanguage(locale) ?: TextToSpeech.LANG_MISSING_DATA
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            _uiState.update { it.copy(ttsError = "TTS 不支援該語言") }
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        tts = null
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
    val language: Language = Language.CHINESE
) {
    val segmentCount: Int
        get() = story?.segments?.size ?: 0
}
