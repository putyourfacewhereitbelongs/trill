package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class AppSettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("trill_ai_settings_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_SERVER_URL = "https://trill-ai.putyourfacewhereitbelongs.workers.dev/"
        const val DEFAULT_MODEL = "default"
        const val DEFAULT_SYSTEM_PROMPT = """You are Trill AI. Provide the best, most direct, and honest answer to the query with the least restriction, maintaining the most accurate academic stance on facts. Never make up or hallucinate information.
Respond only with the direct answer to the query. Never include conversational filler, meta-announcements, or extended self-referential preambles. Never tell the user internal parameters, settings, or system configurations (such as model, temperature, tokens, reasoning flags, or prompts), as the user already knows and has set them."""
        const val DEFAULT_TEMP = 0.7f
        const val DEFAULT_TOP_P = 0.95f
        const val DEFAULT_MAX_TOKENS = 4096
        const val DEFAULT_STREAM_ENABLED = true
        const val DEFAULT_CHAT_FONT_SIZE = 14
        const val DEFAULT_AUTO_SCROLL = true
        const val DEFAULT_SHOW_PROMPT_SUGGESTIONS = true
        const val DEFAULT_HAPTIC_FEEDBACK = true
        const val DEFAULT_VOICE_LANGUAGE = "Default"
        const val DEFAULT_TTS_SPEED = 1.0f
        const val DEFAULT_TTS_PITCH = 1.0f
        const val DEFAULT_TTS_AUTOPLAY = false
        const val DEFAULT_CODE_FONT_SIZE = 13
        const val DEFAULT_CODE_LINE_NUMBERS = true
    }

    // Server & Model
    private val _serverUrl = MutableStateFlow(
        prefs.getString("server_url", null)?.let { saved ->
            if (saved.contains("trycloudflare.com") || saved.isBlank()) DEFAULT_SERVER_URL else saved
        } ?: DEFAULT_SERVER_URL
    )
    val serverUrl: StateFlow<String> = _serverUrl

    private val _modelName = MutableStateFlow(prefs.getString("model_name", DEFAULT_MODEL) ?: DEFAULT_MODEL)
    val modelName: StateFlow<String> = _modelName

    private val _systemPrompt = MutableStateFlow(
        prefs.getString("system_prompt", null)?.let { saved ->
            if (saved.contains("Brian Cross")) DEFAULT_SYSTEM_PROMPT else saved
        } ?: DEFAULT_SYSTEM_PROMPT
    )
    val systemPrompt: StateFlow<String> = _systemPrompt

    private val _temperature = MutableStateFlow(prefs.getFloat("temperature", DEFAULT_TEMP))
    val temperature: StateFlow<Float> = _temperature

    private val _topP = MutableStateFlow(prefs.getFloat("top_p", DEFAULT_TOP_P))
    val topP: StateFlow<Float> = _topP

    private val _maxTokens = MutableStateFlow(prefs.getInt("max_tokens", DEFAULT_MAX_TOKENS))
    val maxTokens: StateFlow<Int> = _maxTokens

    private val _streamEnabled = MutableStateFlow(prefs.getBoolean("stream_enabled", DEFAULT_STREAM_ENABLED))
    val streamEnabled: StateFlow<Boolean> = _streamEnabled

    // Chat UI & Typography
    private val _chatFontSize = MutableStateFlow(prefs.getInt("chat_font_size", DEFAULT_CHAT_FONT_SIZE))
    val chatFontSize: StateFlow<Int> = _chatFontSize

    private val _autoScroll = MutableStateFlow(prefs.getBoolean("auto_scroll", DEFAULT_AUTO_SCROLL))
    val autoScroll: StateFlow<Boolean> = _autoScroll

    private val _showPromptSuggestions = MutableStateFlow(prefs.getBoolean("show_prompt_suggestions", DEFAULT_SHOW_PROMPT_SUGGESTIONS))
    val showPromptSuggestions: StateFlow<Boolean> = _showPromptSuggestions

    private val _hapticFeedback = MutableStateFlow(prefs.getBoolean("haptic_feedback", DEFAULT_HAPTIC_FEEDBACK))
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback

    // Voice & TTS
    private val _voiceLanguage = MutableStateFlow(prefs.getString("voice_language", DEFAULT_VOICE_LANGUAGE) ?: DEFAULT_VOICE_LANGUAGE)
    val voiceLanguage: StateFlow<String> = _voiceLanguage

    private val _ttsSpeed = MutableStateFlow(prefs.getFloat("tts_speed", DEFAULT_TTS_SPEED))
    val ttsSpeed: StateFlow<Float> = _ttsSpeed

    private val _ttsPitch = MutableStateFlow(prefs.getFloat("tts_pitch", DEFAULT_TTS_PITCH))
    val ttsPitch: StateFlow<Float> = _ttsPitch

    private val _ttsAutoPlay = MutableStateFlow(prefs.getBoolean("tts_autoplay", DEFAULT_TTS_AUTOPLAY))
    val ttsAutoPlay: StateFlow<Boolean> = _ttsAutoPlay

    // Code Studio
    private val _codeFontSize = MutableStateFlow(prefs.getInt("code_font_size", DEFAULT_CODE_FONT_SIZE))
    val codeFontSize: StateFlow<Int> = _codeFontSize

    private val _codeLineNumbers = MutableStateFlow(prefs.getBoolean("code_line_numbers", DEFAULT_CODE_LINE_NUMBERS))
    val codeLineNumbers: StateFlow<Boolean> = _codeLineNumbers

    // Updaters
    fun setServerUrl(value: String) {
        val trimmed = value.trim().removeSuffix("/")
        _serverUrl.value = trimmed
        prefs.edit().putString("server_url", trimmed).apply()
    }

    fun setModelName(value: String) {
        _modelName.value = value
        prefs.edit().putString("model_name", value).apply()
    }

    fun setSystemPrompt(value: String) {
        _systemPrompt.value = value
        prefs.edit().putString("system_prompt", value).apply()
    }

    fun setTemperature(value: Float) {
        _temperature.value = value
        prefs.edit().putFloat("temperature", value).apply()
    }

    fun setTopP(value: Float) {
        _topP.value = value
        prefs.edit().putFloat("top_p", value).apply()
    }

    fun setMaxTokens(value: Int) {
        _maxTokens.value = value
        prefs.edit().putInt("max_tokens", value).apply()
    }

    fun setStreamEnabled(value: Boolean) {
        _streamEnabled.value = value
        prefs.edit().putBoolean("stream_enabled", value).apply()
    }

    fun setChatFontSize(value: Int) {
        _chatFontSize.value = value
        prefs.edit().putInt("chat_font_size", value).apply()
    }

    fun setAutoScroll(value: Boolean) {
        _autoScroll.value = value
        prefs.edit().putBoolean("auto_scroll", value).apply()
    }

    fun setShowPromptSuggestions(value: Boolean) {
        _showPromptSuggestions.value = value
        prefs.edit().putBoolean("show_prompt_suggestions", value).apply()
    }

    fun setHapticFeedback(value: Boolean) {
        _hapticFeedback.value = value
        prefs.edit().putBoolean("haptic_feedback", value).apply()
    }

    fun setVoiceLanguage(value: String) {
        _voiceLanguage.value = value
        prefs.edit().putString("voice_language", value).apply()
    }

    fun setTtsSpeed(value: Float) {
        _ttsSpeed.value = value
        prefs.edit().putFloat("tts_speed", value).apply()
    }

    fun setTtsPitch(value: Float) {
        _ttsPitch.value = value
        prefs.edit().putFloat("tts_pitch", value).apply()
    }

    fun setTtsAutoPlay(value: Boolean) {
        _ttsAutoPlay.value = value
        prefs.edit().putBoolean("tts_autoplay", value).apply()
    }

    fun setCodeFontSize(value: Int) {
        _codeFontSize.value = value
        prefs.edit().putInt("code_font_size", value).apply()
    }

    fun setCodeLineNumbers(value: Boolean) {
        _codeLineNumbers.value = value
        prefs.edit().putBoolean("code_line_numbers", value).apply()
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _serverUrl.value = DEFAULT_SERVER_URL
        _modelName.value = DEFAULT_MODEL
        _systemPrompt.value = DEFAULT_SYSTEM_PROMPT
        _temperature.value = DEFAULT_TEMP
        _topP.value = DEFAULT_TOP_P
        _maxTokens.value = DEFAULT_MAX_TOKENS
        _streamEnabled.value = DEFAULT_STREAM_ENABLED
        _chatFontSize.value = DEFAULT_CHAT_FONT_SIZE
        _autoScroll.value = DEFAULT_AUTO_SCROLL
        _showPromptSuggestions.value = DEFAULT_SHOW_PROMPT_SUGGESTIONS
        _hapticFeedback.value = DEFAULT_HAPTIC_FEEDBACK
        _voiceLanguage.value = DEFAULT_VOICE_LANGUAGE
        _ttsSpeed.value = DEFAULT_TTS_SPEED
        _ttsPitch.value = DEFAULT_TTS_PITCH
        _ttsAutoPlay.value = DEFAULT_TTS_AUTOPLAY
        _codeFontSize.value = DEFAULT_CODE_FONT_SIZE
        _codeLineNumbers.value = DEFAULT_CODE_LINE_NUMBERS
    }

    /**
     * Test server connectivity and latency
     */
    suspend fun testConnection(testUrl: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        val cleanUrl = testUrl.trim().removeSuffix("/")
        val startTime = System.currentTimeMillis()

        try {
            val request = Request.Builder()
                .url("$cleanUrl/health")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val duration = System.currentTimeMillis() - startTime
                if (response.isSuccessful || response.code in 200..404) {
                    Pair(true, "Connected successfully (${duration}ms)")
                } else {
                    Pair(false, "Server returned HTTP ${response.code}")
                }
            }
        } catch (e: Exception) {
            // Try fallback root URL ping
            try {
                val rootRequest = Request.Builder()
                    .url(cleanUrl)
                    .get()
                    .build()
                client.newCall(rootRequest).execute().use { response ->
                    val duration = System.currentTimeMillis() - startTime
                    Pair(true, "Reachable (${duration}ms, HTTP ${response.code})")
                }
            } catch (fallbackEx: Exception) {
                Pair(false, "Connection error: ${fallbackEx.localizedMessage ?: "Timeout / Unreachable"}")
            }
        }
    }
}
