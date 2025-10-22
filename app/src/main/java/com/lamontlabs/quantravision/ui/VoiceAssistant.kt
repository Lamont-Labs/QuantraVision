package com.lamontlabs.quantravision.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * VoiceAssistant
 * Optional offline voice feedback for accessibility.
 * Provides deterministic prompts without cloud processing.
 */
class VoiceAssistant(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) tts?.language = Locale.US
    }

    fun speak(text: String) {
        if (!ready) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "qv_voice")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
