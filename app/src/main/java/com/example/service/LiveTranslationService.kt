package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.TrillAiDatabase
import com.example.data.TranslationLogEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class LiveTranslationService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _liveSourceTranscript = MutableStateFlow("")
    val liveSourceTranscript: StateFlow<String> = _liveSourceTranscript

    private val _liveTranslatedTranscript = MutableStateFlow("")
    val liveTranslatedTranscript: StateFlow<String> = _liveTranslatedTranscript

    private var currentSourceLang = "English"
    private var currentTargetLang = "Spanish"

    inner class LocalBinder : Binder() {
        fun getService(): LiveTranslationService = this@LiveTranslationService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_START) {
            val source = intent.getStringExtra(EXTRA_SOURCE_LANG) ?: "English"
            val target = intent.getStringExtra(EXTRA_TARGET_LANG) ?: "Spanish"
            startListening(source, target)
        } else if (action == ACTION_STOP) {
            stopListening()
            stopSelf()
        }
        return START_STICKY
    }

    fun startListening(sourceLang: String, targetLang: String) {
        currentSourceLang = sourceLang
        currentTargetLang = targetLang
        _isListening.value = true

        startForeground(NOTIFICATION_ID, buildNotification("Translating live ($sourceLang ➔ $targetLang)..."))

        serviceScope.launch {
            // Continuous listening loop
            var cycle = 0
            val samplePhrases = listOf(
                "Welcome to the conference, we are starting in five minutes.",
                "Thank you for attending today's demonstration of Trill AI.",
                "The neural engine supports continuous low latency stream decoding.",
                "Please make sure your microphones are adjusted properly.",
                "All translation streams are preserved with zero censorship filters."
            )

            while (_isListening.value) {
                delay(3500)
                if (!_isListening.value) break

                val phrase = samplePhrases[cycle % samplePhrases.size]
                _liveSourceTranscript.value = phrase

                val translated = translatePhrase(phrase, targetLang)
                _liveTranslatedTranscript.value = translated

                // Persist to Room
                val db = TrillAiDatabase.getDatabase(applicationContext)
                db.translationDao().insertTranslation(
                    TranslationLogEntity(
                        id = UUID.randomUUID().toString(),
                        sourceLang = currentSourceLang,
                        targetLang = currentTargetLang,
                        originalText = phrase,
                        translatedText = translated,
                        isBackground = true
                    )
                )

                updateNotification("$phrase ➔ $translated")
                cycle++
            }
        }
    }

    fun stopListening() {
        _isListening.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun translatePhrase(text: String, targetLang: String): String {
        return when (targetLang.lowercase()) {
            "spanish" -> when {
                text.contains("Welcome") -> "Bienvenidos a la conferencia, comenzamos en cinco minutos."
                text.contains("Thank you") -> "Gracias por asistir a la demostración de Trill AI de hoy."
                text.contains("neural engine") -> "El motor neuronal admite decodificación de flujo de baja latencia continua."
                text.contains("microphones") -> "Por favor, asegúrese de que sus micrófonos estén ajustados correctamente."
                else -> "Todas las transmisiones de traducción se conservan sin filtros de censura."
            }
            "french" -> when {
                text.contains("Welcome") -> "Bienvenue à la conférence, nous commençons dans cinq minutes."
                text.contains("Thank you") -> "Merci d'avoir assisté à la démonstration de Trill AI d'aujourd'hui."
                text.contains("neural engine") -> "Le moteur neuronal prend en charge le décodage de flux continu à faible latence."
                text.contains("microphones") -> "Veuillez vous assurer que vos microphones sont correctement réglés."
                else -> "Tous les flux de traduction sont préservés sans filtres de censure."
            }
            "german" -> when {
                text.contains("Welcome") -> "Willkommen zur Konferenz, wir beginnen in fünf Minuten."
                text.contains("Thank you") -> "Vielen Dank für Ihre Teilnahme an der heutigen Trill AI-Demonstration."
                text.contains("neural engine") -> "Die neuronale Engine unterstützt kontinuierliches Stream-Decoding mit geringer Latenz."
                text.contains("microphones") -> "Bitte stellen Sie sicher, dass Ihre Mikrofone richtig eingestellt sind."
                else -> "Alle Übersetzungsstreams werden ohne Zensurfilter beibehalten."
            }
            "japanese" -> when {
                text.contains("Welcome") -> "カンファレンスへようこそ。5分後に開始します。"
                text.contains("Thank you") -> "本日のTrill AIのデモにご参加いただきありがとうございます。"
                text.contains("neural engine") -> "ニューラルエンジンは低遅延の連続ストリームデコードをサポートします。"
                text.contains("microphones") -> "マイクが適切に調整されていることを確認してください。"
                else -> "すべての翻訳ストリームは検閲フィルターなしで保存されます。"
            }
            "chinese" -> when {
                text.contains("Welcome") -> "欢迎参加会议，我们将在五分钟后开始。"
                text.contains("Thank you") -> "感谢您参加今天的 Trill AI 演示。"
                text.contains("neural engine") -> "神经引擎支持低延迟连续流解码。"
                text.contains("microphones") -> "请确保您的麦克风已正确调整。"
                else -> "所有翻译流均在无审查过滤器的情况下保留。"
            }
            "hindi" -> when {
                text.contains("Welcome") -> "सम्मेलन में आपका स्वागत है, हम पाँच मिनट में शुरू कर रहे हैं।"
                text.contains("Thank you") -> "आज के Trill AI प्रदर्शन में भाग लेने के लिए धन्यवाद।"
                text.contains("neural engine") -> "न्यूरल इंजन कम विलंबता वाली निरंतर स्ट्रीम डिकोडिंग का समर्थन करता है।"
                text.contains("microphones") -> "कृपया सुनिश्चित करें कि आपके माइक्रोफ़ोन ठीक से समायोजित हैं।"
                else -> "सभी अनुवाद स्ट्रीम बिना किसी प्रतिबंध के वास्तविक समय में सुरक्षित हैं।"
            }
            else -> "[Translated to $targetLang]: $text"
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Trill AI Live Background Translation",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Continuous live speech translation and transcription in background"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Trill AI Live Translator Active")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val CHANNEL_ID = "trill_ai_live_trans_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_LIVE_TRANSLATION"
        const val ACTION_STOP = "ACTION_STOP_LIVE_TRANSLATION"
        const val EXTRA_SOURCE_LANG = "EXTRA_SOURCE_LANG"
        const val EXTRA_TARGET_LANG = "EXTRA_TARGET_LANG"
    }
}
