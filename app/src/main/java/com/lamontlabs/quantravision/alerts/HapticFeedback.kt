package com.lamontlabs.quantravision.alerts

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class HapticFeedback(private val context: Context) {

    private val vibrator: Vibrator? = getVibratorService()
    private val isEnabled = AtomicBoolean(true)
    
    private fun getVibratorService(): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Timber.e(e, "HapticFeedback: Error getting vibrator service")
            null
        }
    }
    
    fun vibrateForBullishPattern() {
        if (!isEnabled.get()) return
        
        vibrate(
            pattern = longArrayOf(0, 100, 100, 100),
            amplitudes = intArrayOf(0, 150, 0, 150),
            description = "Bullish pattern (2 short buzzes)"
        )
    }
    
    fun vibrateForBearishPattern() {
        if (!isEnabled.get()) return
        
        vibrate(
            pattern = longArrayOf(0, 100, 100, 100, 100, 100),
            amplitudes = intArrayOf(0, 150, 0, 150, 0, 150),
            description = "Bearish pattern (3 short buzzes)"
        )
    }
    
    fun vibrateForHighConfidence() {
        if (!isEnabled.get()) return
        
        vibrate(
            pattern = longArrayOf(0, 400),
            amplitudes = intArrayOf(0, 200),
            description = "High confidence (long buzz)"
        )
    }
    
    fun vibrateForInvalidation() {
        if (!isEnabled.get()) return
        
        vibrate(
            pattern = longArrayOf(0, 300, 200, 300),
            amplitudes = intArrayOf(0, 255, 0, 255),
            description = "Pattern invalidated (double long buzz)"
        )
    }
    
    fun vibrateForWatchlistAlert(patternCount: Int) {
        if (!isEnabled.get()) return
        
        val repetitions = minOf(patternCount, 5)
        val pattern = LongArray(repetitions * 2) { i ->
            if (i % 2 == 0) 0 else 150
        }
        val amplitudes = IntArray(repetitions * 2) { i ->
            if (i % 2 == 0) 0 else 180
        }
        
        vibrate(
            pattern = pattern,
            amplitudes = amplitudes,
            description = "Watchlist alert ($patternCount patterns)"
        )
    }
    
    fun vibrateCustom(strength: PatternStrength.StrengthLevel) {
        if (!isEnabled.get()) return
        
        when (strength) {
            PatternStrength.StrengthLevel.WEAK -> {
                vibrate(
                    pattern = longArrayOf(0, 100),
                    amplitudes = intArrayOf(0, 100),
                    description = "Weak pattern"
                )
            }
            PatternStrength.StrengthLevel.MODERATE -> {
                vibrate(
                    pattern = longArrayOf(0, 150),
                    amplitudes = intArrayOf(0, 150),
                    description = "Moderate pattern"
                )
            }
            PatternStrength.StrengthLevel.STRONG -> {
                vibrate(
                    pattern = longArrayOf(0, 250),
                    amplitudes = intArrayOf(0, 200),
                    description = "Strong pattern"
                )
            }
        }
    }
    
    private fun vibrate(pattern: LongArray, amplitudes: IntArray, description: String) {
        try {
            if (vibrator?.hasVibrator() != true) {
                Timber.w("HapticFeedback: No vibrator available")
                return
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
                vibrator.vibrate(effect)
                Timber.d("HapticFeedback: $description")
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
                Timber.d("HapticFeedback: $description (legacy mode)")
            }
        } catch (e: Exception) {
            Timber.e(e, "HapticFeedback: Error vibrating")
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        isEnabled.set(enabled)
        if (!enabled) {
            cancel()
        }
        Timber.d("HapticFeedback: ${if (enabled) "Enabled" else "Disabled"}")
    }
    
    fun cancel() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Timber.e(e, "HapticFeedback: Error canceling vibration")
        }
    }
    
    fun isHapticEnabled(): Boolean = isEnabled.get()
    
    fun hasVibrator(): Boolean = vibrator?.hasVibrator() == true
}
