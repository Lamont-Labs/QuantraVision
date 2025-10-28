package com.lamontlabs.quantravision.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.lamontlabs.quantravision.PatternMatch

/**
 * EnhancedVoiceCommands
 * Hands-free operation with natural language pattern commands
 */
object EnhancedVoiceCommands {

    data class VoiceCommand(
        val action: String,
        val parameters: Map<String, String>
    )

    fun parseCommand(speechText: String): VoiceCommand? {
        val text = speechText.lowercase()

        return when {
            text.contains("show") && text.contains("bull") && text.contains("flag") -> {
                VoiceCommand("filter_pattern", mapOf("pattern" to "bull_flag"))
            }
            text.contains("show") && text.contains("bear") && text.contains("flag") -> {
                VoiceCommand("filter_pattern", mapOf("pattern" to "bear_flag"))
            }
            text.contains("show") && text.contains("head") && text.contains("shoulder") -> {
                VoiceCommand("filter_pattern", mapOf("pattern" to "head_shoulders"))
            }
            text.contains("show") && text.contains("double") && text.contains("top") -> {
                VoiceCommand("filter_pattern", mapOf("pattern" to "double_top"))
            }
            text.contains("show") && text.contains("double") && text.contains("bottom") -> {
                VoiceCommand("filter_pattern", mapOf("pattern" to "double_bottom"))
            }
            text.contains("export") && (text.contains("last") || text.contains("latest")) -> {
                VoiceCommand("export_last", emptyMap())
            }
            text.contains("export") && text.contains("all") -> {
                VoiceCommand("export_all", emptyMap())
            }
            text.contains("start") && text.contains("scan") -> {
                VoiceCommand("start_scan", emptyMap())
            }
            text.contains("stop") && text.contains("scan") -> {
                VoiceCommand("stop_scan", emptyMap())
            }
            text.contains("clear") && text.contains("overlay") -> {
                VoiceCommand("clear_overlay", emptyMap())
            }
            text.contains("show") && text.contains("all") -> {
                VoiceCommand("show_all", emptyMap())
            }
            text.contains("high") && text.contains("confidence") -> {
                VoiceCommand("filter_confidence", mapOf("threshold" to "0.85"))
            }
            text.contains("show") && text.contains("statistic") -> {
                VoiceCommand("show_statistics", emptyMap())
            }
            text.contains("what") && (text.contains("detect") || text.contains("found")) -> {
                VoiceCommand("list_detections", emptyMap())
            }
            text.contains("help") -> {
                VoiceCommand("show_help", emptyMap())
            }
            text.contains("settings") -> {
                VoiceCommand("open_settings", emptyMap())
            }
            else -> null
        }
    }

    fun getSupportedCommands(): List<String> {
        return listOf(
            "Show bull flags",
            "Show bear flags",
            "Show head and shoulders",
            "Show double tops",
            "Show double bottoms",
            "Export last detection",
            "Export all detections",
            "Start scanning",
            "Stop scanning",
            "Clear overlay",
            "Show all patterns",
            "Show high confidence patterns",
            "Show statistics",
            "What did you detect?",
            "Open settings",
            "Help"
        )
    }

    fun getCommandDescription(command: String): String {
        return when (command) {
            "Show bull flags" -> "Filter and display only bull flag patterns"
            "Show bear flags" -> "Filter and display only bear flag patterns"
            "Show head and shoulders" -> "Display head and shoulders patterns"
            "Show double tops" -> "Display double top reversal patterns"
            "Show double bottoms" -> "Display double bottom reversal patterns"
            "Export last detection" -> "Export the most recent pattern detection"
            "Export all detections" -> "Export all detected patterns as PDF"
            "Start scanning" -> "Begin real-time pattern detection"
            "Stop scanning" -> "Pause pattern detection"
            "Clear overlay" -> "Remove all pattern highlights from screen"
            "Show all patterns" -> "Display all detected patterns"
            "Show high confidence patterns" -> "Filter patterns with 85%+ confidence"
            "Show statistics" -> "Open pattern analytics dashboard"
            "What did you detect?" -> "List all current pattern detections"
            "Open settings" -> "Navigate to app settings"
            "Help" -> "Show available voice commands"
            else -> ""
        }
    }

    fun createSpeechRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }
}
