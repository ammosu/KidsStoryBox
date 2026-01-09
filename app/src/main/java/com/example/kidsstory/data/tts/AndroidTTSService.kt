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
 * TTS 播放事件監聽器
 */
interface TTSPlaybackListener {
    /**
     * 當段落播放完成時調用
     */
    fun onSegmentComplete(utteranceId: String)

    /**
     * 當播放發生錯誤時調用
     */
    fun onPlaybackError(utteranceId: String?, error: String)
}

/**
 * Android 系統 TTS 服務
 * 提供文字轉語音功能
 */
@Singleton
class AndroidTTSService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AndroidTTSService"
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var playbackListener: TTSPlaybackListener? = null

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
                android.util.Log.d(TAG, "TTS initialized successfully")
            } else {
                android.util.Log.e(TAG, "TTS initialization failed with status: $status")
                _playbackState.value = _playbackState.value.copy(
                    error = "TTS 初始化失敗"
                )
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                android.util.Log.d(TAG, "Utterance started: $utteranceId")
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = true,
                    currentUtteranceId = utteranceId,
                    error = null
                )
            }

            override fun onDone(utteranceId: String?) {
                android.util.Log.d(TAG, "Utterance completed: $utteranceId")
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    currentUtteranceId = null
                )

                // 通知監聽器段落播放完成
                utteranceId?.let { id ->
                    playbackListener?.onSegmentComplete(id)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                val errorMsg = "TTS 發生錯誤"
                android.util.Log.e(TAG, "Utterance error: $utteranceId")
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    error = errorMsg
                )

                // 通知監聽器發生錯誤
                playbackListener?.onPlaybackError(utteranceId, errorMsg)
            }
        })
    }

    /**
     * 設定播放事件監聽器
     */
    fun setPlaybackListener(listener: TTSPlaybackListener?) {
        playbackListener = listener
    }

    /**
     * 播放文字
     * @param text 要播放的文字內容
     * @param utteranceId 唯一識別碼，用於追蹤播放進度
     * @return 是否成功開始播放
     */
    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()): Boolean {
        if (!isInitialized) {
            val errorMsg = "TTS 尚未初始化"
            android.util.Log.e(TAG, errorMsg)
            _playbackState.value = _playbackState.value.copy(error = errorMsg)
            playbackListener?.onPlaybackError(utteranceId, errorMsg)
            return false
        }

        if (text.isBlank()) {
            android.util.Log.w(TAG, "Attempted to speak empty text")
            return false
        }

        android.util.Log.d(TAG, "Speaking text with utteranceId: $utteranceId")
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        return result == TextToSpeech.SUCCESS
    }

    /**
     * 停止播放
     */
    fun stop() {
        android.util.Log.d(TAG, "Stopping TTS playback")
        tts?.stop()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            currentUtteranceId = null,
            error = null
        )
    }

    /**
     * 設定語言
     * @param languageCode 語言代碼 ("zh" 或 "en")
     * @return 是否成功設定語言
     */
    fun setLanguage(languageCode: String): Boolean {
        val locale = when (languageCode) {
            "zh" -> Locale.TAIWAN
            "en" -> Locale.US
            else -> Locale.TAIWAN
        }

        android.util.Log.d(TAG, "Setting language to: $languageCode")
        val result = tts?.setLanguage(locale)

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            val errorMsg = "TTS 不支援該語言: $languageCode"
            android.util.Log.e(TAG, errorMsg)
            _playbackState.value = _playbackState.value.copy(error = errorMsg)
            return false
        }

        return true
    }

    /**
     * 設定語速
     * @param rate 語速 (0.5 - 1.5)
     */
    fun setSpeechRate(rate: Float) {
        val clampedRate = rate.coerceIn(0.5f, 1.5f)
        android.util.Log.d(TAG, "Setting speech rate to: $clampedRate")
        tts?.setSpeechRate(clampedRate)
    }

    /**
     * 設定音調
     * @param pitch 音調 (0.5 - 1.5)
     */
    fun setPitch(pitch: Float) {
        val clampedPitch = pitch.coerceIn(0.5f, 1.5f)
        android.util.Log.d(TAG, "Setting pitch to: $clampedPitch")
        tts?.setPitch(clampedPitch)
    }

    /**
     * 檢查 TTS 是否已初始化並可用
     */
    fun isReady(): Boolean = isInitialized

    /**
     * 釋放資源
     */
    fun shutdown() {
        android.util.Log.d(TAG, "Shutting down TTS service")
        playbackListener = null
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
