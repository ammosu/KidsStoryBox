package com.example.kidsstory.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android 系統 TTS 服務
 * 提供文字轉語音功能
 */
@Singleton
class AndroidTTSService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _playbackState = MutableStateFlow(TTSPlaybackState())
    val playbackState: StateFlow<TTSPlaybackState> = _playbackState.asStateFlow()

    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                // 設定預設語言為中文
                tts?.language = Locale.TRADITIONAL_CHINESE
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = true,
                    currentUtteranceId = utteranceId
                )
            }

            override fun onDone(utteranceId: String?) {
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    currentUtteranceId = null
                )
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    error = "TTS 發生錯誤"
                )
            }
        })
    }

    /**
     * 播放文字
     */
    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()) {
        if (!isInitialized) {
            _playbackState.value = _playbackState.value.copy(
                error = "TTS 尚未初始化"
            )
            return
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * 停止播放
     */
    fun stop() {
        tts?.stop()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            currentUtteranceId = null
        )
    }

    /**
     * 設定語言
     */
    fun setLanguage(languageCode: String) {
        val locale = when (languageCode) {
            "zh" -> Locale.TRADITIONAL_CHINESE
            "en" -> Locale.US
            else -> Locale.TRADITIONAL_CHINESE
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            _playbackState.value = _playbackState.value.copy(
                error = "不支援此語言"
            )
        }
    }

    /**
     * 設定語速
     */
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    /**
     * 設定音調
     */
    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    /**
     * 釋放資源
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}

/**
 * TTS 播放狀態
 */
data class TTSPlaybackState(
    val isPlaying: Boolean = false,
    val currentUtteranceId: String? = null,
    val error: String? = null
)
