package com.example.iqromandarin.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import java.util.Locale

/**
 * Handles all audio playback:
 * 1. Raw resource audio (pre-recorded native speaker)
 * 2. TTS (Chinese TextToSpeech fallback)
 */
class AudioHandler(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var exoPlayer: ExoPlayer? = null

    init {
        initTTS()
        initExoPlayer()
    }

    private fun initTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                tts?.setSpeechRate(0.85f) // Slightly slower for learners
                tts?.setPitch(1.0f)
            }
        }
    }

    private fun initExoPlayer() {
        exoPlayer = ExoPlayer.Builder(context).build()
    }

    /**
     * Speak text using Chinese TTS.
     * Used as fallback when no raw audio available.
     */
    fun speakChinese(text: String) {
        if (!ttsReady || tts == null) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "iqro_${System.currentTimeMillis()}")
    }

    /**
     * Play audio from raw resource (pre-recorded Chinese audio).
     * Format: R.raw.audio_ba, R.raw.audio_ma, etc.
     */
    fun playRawAudio(rawResId: Int) {
        try {
            exoPlayer?.stop()
            val uri = android.net.Uri.parse("android.resource://${context.packageName}/$rawResId")
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            exoPlayer?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Play audio from file path (recorded user audio)
     */
    fun playFromFile(filePath: String) {
        try {
            exoPlayer?.stop()
            val mediaItem = MediaItem.fromUri(android.net.Uri.parse(filePath))
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            exoPlayer?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopPlayback() {
        exoPlayer?.stop()
        tts?.stop()
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        tts?.shutdown()
        tts = null
    }

    fun isTTSAvailable(): Boolean = ttsReady
}
