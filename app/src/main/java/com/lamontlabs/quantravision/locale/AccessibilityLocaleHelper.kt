package com.lamontlabs.quantravision.locale

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.core.os.LocaleListCompat
import com.lamontlabs.quantravision.analysis.TradeabilityLabel
import com.lamontlabs.quantravision.detection.Detection
import java.util.Locale

/**
 * Accessibility + Locale helper
 * - System locale aware, optional in-app override via setAppLocale(tag)
 * - Generates TalkBack-friendly labels for detections
 * - Announces important HUD changes using AccessibilityManager
 */
object AccessibilityLocaleHelper {

    // ----- Locale management -----
    @Volatile private var appLocale: Locale? = null

    /** Set app-specific locale (e.g., "en", "es", "ru", "ja"). Pass null to follow system. */
    fun setAppLocale(tag: String?) {
        appLocale = tag?.let { Locale.forLanguageTag(it) }
    }

    /** Returns the active Locale (override or system). */
    fun activeLocale(context: Context): Locale {
        return appLocale ?: if (Build.VERSION.SDK_INT >= 24) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }

    /** Wrap a string for current locale if you render dynamic text manually. */
    fun localize(context: Context, text: String): String = text

    /** Returns a Context configured to the chosen app locale for resource resolution. */
    fun contextWithLocale(base: Context): Context {
        val loc = appLocale ?: return base
        val conf = base.resources.configuration
        if (Build.VERSION.SDK_INT >= 24) {
            conf.setLocales(android.os.LocaleList(loc))
        } else {
            @Suppress("DEPRECATION")
            conf.locale = loc
        }
        @Suppress("DEPRECATION")
        return base.createConfigurationContext(conf)
    }

    // ----- Accessibility labeling -----

    /** Build a TalkBack label, e.g., "Head and Shoulders, 84 percent, Viable". */
    fun detectionA11yLabel(det: Detection, tradeLabel: TradeabilityLabel?): String {
        val pct = det.confidence.coerceIn(0, 100)
        val status = when (tradeLabel) {
            TradeabilityLabel.VIABLE -> "Viable"
            TradeabilityLabel.CAUTION -> "Caution"
            TradeabilityLabel.NOT_VIABLE -> "Not viable"
            null -> ""
        }
        return if (status.isNotEmpty())
            "${det.name}, $pct percent, $status"
        else
            "${det.name}, $pct percent"
    }

    /** Mark a view as important for accessibility and set its content description. */
    fun setContentDesc(view: View, desc: String) {
        view.contentDescription = desc
        view.isImportantForAccessibility = true
    }

    /** Announce a transient message to TalkBack users. */
    fun announce(view: View, message: String) {
        val am = view.context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (!am.isEnabled) return
        val ev = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        ev.text.add(message)
        ev.className = view.javaClass.name
        ev.packageName = view.context.packageName
        ev.isEnabled = true
        view.parent?.requestSendAccessibilityEvent(view, ev)
    }

    // ----- Optional: simple TTS fallback (offline) -----
    private var tts: TextToSpeech? = null
    fun speak(context: Context, message: String) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = activeLocale(context)
                    tts?.speak(message, TextToSpeech.QUEUE_FLUSH, Bundle(), "qv_a11y")
                }
            }
        } else {
            tts?.language = activeLocale(context)
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, Bundle(), "qv_a11y")
        }
    }

    fun shutdownTts() {
        try { tts?.shutdown() } catch (_: Throwable) {}
        tts = null
    }
}
