package com.lamontlabs.quantravision.alerts

import android.content.Context
import android.content.SharedPreferences
import com.lamontlabs.quantravision.InvalidatedPattern
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.detection.InvalidationDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class AlertManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var instance: AlertManager? = null
        
        private const val PREFS_NAME = "alert_manager_prefs"
        private const val KEY_VOICE_ENABLED = "voice_enabled"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        
        fun getInstance(context: Context): AlertManager {
            return instance ?: synchronized(this) {
                instance ?: AlertManager(context.applicationContext).also { 
                    instance = it
                    Timber.d("AlertManager: Singleton instance created")
                }
            }
        }
        
        fun shutdownInstance() {
            synchronized(this) {
                instance?.shutdown()
                instance = null
                Timber.d("AlertManager: Singleton instance destroyed")
            }
        }
    }

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val voiceAnnouncer = VoiceAnnouncer(appContext)
    private val hapticFeedback = HapticFeedback(appContext)
    private val db = PatternDatabase.getInstance(appContext)
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val patternHistory = ConcurrentHashMap<String, Double>()
    
    private val voiceEnabled = AtomicBoolean(prefs.getBoolean(KEY_VOICE_ENABLED, true))
    private val hapticEnabled = AtomicBoolean(prefs.getBoolean(KEY_HAPTIC_ENABLED, true))
    
    init {
        val voice = voiceEnabled.get()
        val haptic = hapticEnabled.get()
        voiceAnnouncer.setEnabled(voice)
        hapticFeedback.setEnabled(haptic)
        Timber.d("AlertManager: Initialized with voice=$voice, haptic=$haptic")
    }
    
    fun onPatternDetected(pattern: PatternMatch) {
        scope.launch {
            try {
                val strength = PatternStrength.calculateStrength(pattern.confidence)
                val strengthInfo = PatternStrength.getStrengthInfo(pattern.confidence)
                val previousConf = patternHistory[pattern.patternName]
                
                checkInvalidation(pattern, previousConf)
                
                patternHistory[pattern.patternName] = pattern.confidence
                
                if (PatternStrength.shouldAnnounce(pattern.confidence, previousConf)) {
                    announcePattern(pattern, strength, strengthInfo)
                }
                
                triggerHapticFeedback(pattern, strength)
                
                Timber.d("AlertManager: Pattern alert processed - ${pattern.patternName} (${strengthInfo.label})")
            } catch (e: Exception) {
                Timber.e(e, "AlertManager: Error processing pattern detection")
            }
        }
    }
    
    private fun announcePattern(
        pattern: PatternMatch,
        strength: PatternStrength.StrengthLevel,
        strengthInfo: PatternStrength.StrengthInfo
    ) {
        if (!voiceEnabled.get() || !voiceAnnouncer.isReady()) return
        
        val formationPercent = PatternStrength.getFormationPercent(pattern.confidence)
        
        if (pattern.confidence >= 0.90) {
            voiceAnnouncer.announceHighConfidencePattern(pattern.patternName, pattern.confidence)
        } else {
            voiceAnnouncer.announcePatternDetected(
                patternName = pattern.patternName,
                confidence = pattern.confidence,
                strength = strength,
                formationPercent = if (formationPercent < 100) formationPercent else null
            )
        }
    }
    
    private fun triggerHapticFeedback(
        pattern: PatternMatch,
        strength: PatternStrength.StrengthLevel
    ) {
        if (!hapticEnabled.get() || !hapticFeedback.hasVibrator()) return
        
        when {
            pattern.confidence >= 0.85 -> hapticFeedback.vibrateForHighConfidence()
            isBullishPattern(pattern.patternName) -> hapticFeedback.vibrateForBullishPattern()
            isBearishPattern(pattern.patternName) -> hapticFeedback.vibrateForBearishPattern()
            else -> hapticFeedback.vibrateCustom(strength)
        }
    }
    
    private suspend fun checkInvalidation(pattern: PatternMatch, previousConf: Double?) {
        val invalidation = InvalidationDetector.checkInvalidation(pattern, previousConf)
        
        if (invalidation.isInvalidated && invalidation.reason != null) {
            handleInvalidation(pattern, previousConf ?: 0.0, invalidation.reason)
        }
    }
    
    private suspend fun handleInvalidation(
        pattern: PatternMatch,
        previousConf: Double,
        reason: String
    ) {
        try {
            val invalidatedPattern = InvalidatedPattern(
                patternName = pattern.patternName,
                previousConfidence = previousConf,
                finalConfidence = pattern.confidence,
                invalidationReason = reason,
                timestamp = System.currentTimeMillis(),
                timeframe = pattern.timeframe
            )
            
            db.invalidatedPatternDao().insert(invalidatedPattern)
            
            if (voiceEnabled.get() && voiceAnnouncer.isReady()) {
                voiceAnnouncer.announcePatternInvalidated(pattern.patternName, reason)
            }
            
            if (hapticEnabled.get() && hapticFeedback.hasVibrator()) {
                hapticFeedback.vibrateForInvalidation()
            }
            
            patternHistory.remove(pattern.patternName)
            
            Timber.i("AlertManager: Pattern invalidated - ${pattern.patternName} ($reason)")
        } catch (e: Exception) {
            Timber.e(e, "AlertManager: Error handling invalidation")
        }
    }
    
    fun onWatchlistAlert(symbolCount: Int, patternType: String) {
        scope.launch {
            if (voiceEnabled.get() && voiceAnnouncer.isReady()) {
                voiceAnnouncer.announceWatchlistAlert(symbolCount, patternType)
            }
            
            if (hapticEnabled.get() && hapticFeedback.hasVibrator()) {
                hapticFeedback.vibrateForWatchlistAlert(symbolCount)
            }
        }
    }
    
    private fun isBullishPattern(patternName: String): Boolean {
        val bullishKeywords = listOf(
            "bull", "ascending", "cup", "inverse head",
            "double bottom", "rising", "bullish"
        )
        return bullishKeywords.any { patternName.contains(it, ignoreCase = true) }
    }
    
    private fun isBearishPattern(patternName: String): Boolean {
        val bearishKeywords = listOf(
            "bear", "descending", "head & shoulders", "head and shoulders",
            "double top", "falling", "bearish"
        )
        return bearishKeywords.any { patternName.contains(it, ignoreCase = true) }
    }
    
    fun setVoiceEnabled(enabled: Boolean) {
        voiceEnabled.set(enabled)
        voiceAnnouncer.setEnabled(enabled)
        prefs.edit().putBoolean(KEY_VOICE_ENABLED, enabled).apply()
        Timber.d("AlertManager: Voice announcements ${if (enabled) "enabled" else "disabled"} (persisted)")
    }
    
    fun setHapticEnabled(enabled: Boolean) {
        hapticEnabled.set(enabled)
        hapticFeedback.setEnabled(enabled)
        prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply()
        Timber.d("AlertManager: Haptic feedback ${if (enabled) "enabled" else "disabled"} (persisted)")
    }
    
    fun isVoiceEnabled(): Boolean = voiceEnabled.get()
    
    fun isHapticEnabled(): Boolean = hapticEnabled.get()
    
    fun stop() {
        voiceAnnouncer.stop()
        hapticFeedback.cancel()
    }
    
    fun shutdown() {
        voiceAnnouncer.shutdown()
        Timber.d("AlertManager: Shutdown complete")
    }
}
