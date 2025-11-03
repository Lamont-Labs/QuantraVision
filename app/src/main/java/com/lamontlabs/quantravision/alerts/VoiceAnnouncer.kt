package com.lamontlabs.quantravision.alerts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class VoiceAnnouncer(private val context: Context) {

    private var tts: TextToSpeech? = null
    private val isInitialized = AtomicBoolean(false)
    private val isEnabled = AtomicBoolean(true)
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(1.0f)
                tts?.setPitch(1.0f)
                isInitialized.set(true)
                Timber.d("VoiceAnnouncer: TTS initialized successfully")
            } else {
                Timber.w("VoiceAnnouncer: TTS initialization failed with status $status")
            }
        }
        
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Timber.d("VoiceAnnouncer: Started speaking: $utteranceId")
            }
            
            override fun onDone(utteranceId: String?) {
                Timber.d("VoiceAnnouncer: Finished speaking: $utteranceId")
            }
            
            override fun onError(utteranceId: String?) {
                Timber.e("VoiceAnnouncer: Error speaking: $utteranceId")
            }
        })
    }
    
    fun announcePatternDetected(
        patternName: String,
        confidence: Double,
        strength: PatternStrength.StrengthLevel,
        formationPercent: Int? = null
    ) {
        if (!isEnabled.get() || !isInitialized.get()) return
        
        val strengthWord = when (strength) {
            PatternStrength.StrengthLevel.WEAK -> "weak"
            PatternStrength.StrengthLevel.MODERATE -> "moderate"
            PatternStrength.StrengthLevel.STRONG -> "strong"
        }
        
        val message = if (formationPercent != null && formationPercent < 100) {
            "$patternName forming, $formationPercent percent complete, $strengthWord confidence"
        } else {
            "$patternName detected, $strengthWord confidence"
        }
        
        speak(message, "detection_${System.currentTimeMillis()}")
    }
    
    fun announcePatternInvalidated(patternName: String, reason: String) {
        if (!isEnabled.get() || !isInitialized.get()) return
        
        val message = "$patternName invalidated, $reason"
        speak(message, "invalidation_${System.currentTimeMillis()}", urgent = true)
    }
    
    fun announceWatchlistAlert(symbolCount: Int, patternType: String) {
        if (!isEnabled.get() || !isInitialized.get()) return
        
        val message = "$symbolCount stocks showing $patternType patterns"
        speak(message, "watchlist_${System.currentTimeMillis()}")
    }
    
    fun announceHighConfidencePattern(patternName: String, confidence: Double) {
        if (!isEnabled.get() || !isInitialized.get()) return
        
        val confidencePercent = (confidence * 100).toInt()
        val message = "High confidence $patternName, $confidencePercent percent"
        speak(message, "high_conf_${System.currentTimeMillis()}", urgent = true)
    }
    
    private fun speak(text: String, utteranceId: String, urgent: Boolean = false) {
        try {
            val queueMode = if (urgent) {
                TextToSpeech.QUEUE_FLUSH
            } else {
                TextToSpeech.QUEUE_ADD
            }
            
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            
            tts?.speak(text, queueMode, params, utteranceId)
            Timber.d("VoiceAnnouncer: Speaking: $text")
        } catch (e: Exception) {
            Timber.e(e, "VoiceAnnouncer: Error speaking text")
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        isEnabled.set(enabled)
        if (!enabled) {
            stop()
        }
        Timber.d("VoiceAnnouncer: ${if (enabled) "Enabled" else "Disabled"}")
    }
    
    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Timber.e(e, "VoiceAnnouncer: Error stopping TTS")
        }
    }
    
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            isInitialized.set(false)
            Timber.d("VoiceAnnouncer: Shutdown complete")
        } catch (e: Exception) {
            Timber.e(e, "VoiceAnnouncer: Error during shutdown")
        }
    }
    
    fun isReady(): Boolean = isInitialized.get()
    
    fun isVoiceEnabled(): Boolean = isEnabled.get()
}
