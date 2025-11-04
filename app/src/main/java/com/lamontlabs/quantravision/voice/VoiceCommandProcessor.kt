package com.lamontlabs.quantravision.voice

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.analysis.NotebookExporter
import java.io.File

sealed class VoiceCommandResult {
    data class FilterPattern(val patternType: String) : VoiceCommandResult()
    object ClearFilter : VoiceCommandResult()
    object ExportPDF : VoiceCommandResult()
    object StartScanning : VoiceCommandResult()
    object StopScanning : VoiceCommandResult()
    data class FilterByConfidence(val threshold: Double) : VoiceCommandResult()
    data class FilterByTime(val minutesAgo: Int) : VoiceCommandResult()
    object ClearHighlights : VoiceCommandResult()
    object RefreshDetection : VoiceCommandResult()
    object NavigateAchievements : VoiceCommandResult()
    object NavigateAnalytics : VoiceCommandResult()
    object NavigatePredictions : VoiceCommandResult()
    object NavigateLearning : VoiceCommandResult()
    object NavigateAdvancedLearning : VoiceCommandResult()
    object NavigateExport : VoiceCommandResult()
    object NavigatePerformance : VoiceCommandResult()
    object NavigateAbout : VoiceCommandResult()
    data class VerbalSummary(val summary: String) : VoiceCommandResult()
    data class ShowHelp(val commands: List<String>) : VoiceCommandResult()
    object CommandNotRecognized : VoiceCommandResult()
}

object VoiceCommandProcessor {
    
    private val bullFlagPatterns = setOf("show bull flag", "show bullish flag", "display bull flags", 
        "show bull flags", "filter bull flag", "bullish flags")
    
    private val bearFlagPatterns = setOf("show bear flag", "show bearish flag", "display bear flags",
        "show bear flags", "filter bear flag", "bearish flags")
    
    private val allPatternsCommands = setOf("show all patterns", "display all patterns", 
        "show all", "clear filter", "show everything", "display everything")
    
    private val exportCommands = setOf("export all detections", "export to pdf", "export all",
        "generate pdf", "create pdf", "export pdf", "export report")
    
    private val startScanCommands = setOf("start scanning", "begin scanning", "start detection",
        "begin detection", "start scan", "scan now")
    
    private val stopScanCommands = setOf("stop scanning", "pause scanning", "stop detection",
        "pause detection", "stop scan", "pause scan")
    
    private val summaryCommands = setOf("what did you detect", "what did you find", 
        "show detections", "list detections", "what patterns", "summary")
    
    private val highConfidenceCommands = setOf("show high confidence", "high confidence only",
        "show confident patterns", "filter by confidence", "best patterns")
    
    private val recentPatternsCommands = setOf("show recent patterns", "recent patterns",
        "show recent", "latest patterns", "new patterns")
    
    private val clearHighlightsCommands = setOf("clear highlights", "remove highlights",
        "clear all", "hide highlights", "remove all")
    
    private val refreshCommands = setOf("refresh detection", "re-scan", "rescan",
        "scan again", "refresh scan", "update detection")
    
    private val achievementsCommands = setOf("show achievements", "open achievements",
        "achievements", "view achievements", "my achievements")
    
    private val analyticsCommands = setOf("show analytics", "open analytics",
        "analytics", "view analytics", "pattern analytics")
    
    private val predictionsCommands = setOf("show predictions", "open predictions",
        "predictions", "view predictions", "pattern predictions")
    
    private val learningCommands = setOf("show learning", "open learning", 
        "show learning analytics", "learning dashboard")
    
    private val advancedLearningCommands = setOf("show advanced learning", 
        "open advanced learning", "advanced analytics")
    
    private val exportCenterCommands = setOf("open export", "show export center", 
        "export center")
    
    private val performanceCommands = setOf("show performance", "open performance", 
        "performance dashboard", "show metrics")
    
    private val aboutCommands = setOf("show about", "about app", 
        "app information", "app info")
    
    private val helpCommands = setOf("help", "show help", "available commands",
        "what can you do", "voice commands")
    
    fun processCommand(
        speechText: String,
        context: Context,
        currentDetections: List<PatternMatch> = emptyList()
    ): VoiceCommandResult {
        val normalized = speechText.lowercase().trim()
        
        return when {
            matchesAny(normalized, bullFlagPatterns) -> 
                VoiceCommandResult.FilterPattern("bull_flag")
            
            matchesAny(normalized, bearFlagPatterns) -> 
                VoiceCommandResult.FilterPattern("bear_flag")
            
            matchesAny(normalized, allPatternsCommands) -> 
                VoiceCommandResult.ClearFilter
            
            matchesAny(normalized, exportCommands) -> {
                exportAllDetections(context)
                VoiceCommandResult.ExportPDF
            }
            
            matchesAny(normalized, startScanCommands) -> 
                VoiceCommandResult.StartScanning
            
            matchesAny(normalized, stopScanCommands) -> 
                VoiceCommandResult.StopScanning
            
            matchesAny(normalized, summaryCommands) -> 
                generateVerbalSummary(currentDetections)
            
            matchesAny(normalized, highConfidenceCommands) -> 
                VoiceCommandResult.FilterByConfidence(0.75)
            
            matchesAny(normalized, recentPatternsCommands) -> 
                VoiceCommandResult.FilterByTime(5)
            
            matchesAny(normalized, clearHighlightsCommands) -> 
                VoiceCommandResult.ClearHighlights
            
            matchesAny(normalized, refreshCommands) -> 
                VoiceCommandResult.RefreshDetection
            
            matchesAny(normalized, achievementsCommands) -> 
                VoiceCommandResult.NavigateAchievements
            
            matchesAny(normalized, analyticsCommands) -> 
                VoiceCommandResult.NavigateAnalytics
            
            matchesAny(normalized, predictionsCommands) -> 
                VoiceCommandResult.NavigatePredictions
            
            matchesAny(normalized, learningCommands) -> 
                VoiceCommandResult.NavigateLearning
            
            matchesAny(normalized, advancedLearningCommands) -> 
                VoiceCommandResult.NavigateAdvancedLearning
            
            matchesAny(normalized, exportCenterCommands) -> 
                VoiceCommandResult.NavigateExport
            
            matchesAny(normalized, performanceCommands) -> 
                VoiceCommandResult.NavigatePerformance
            
            matchesAny(normalized, aboutCommands) -> 
                VoiceCommandResult.NavigateAbout
            
            matchesAny(normalized, helpCommands) -> 
                VoiceCommandResult.ShowHelp(getAllCommands())
            
            else -> VoiceCommandResult.CommandNotRecognized
        }
    }
    
    private fun matchesAny(text: String, patterns: Set<String>): Boolean {
        return patterns.any { pattern -> 
            text.contains(pattern) || 
            text.replace("\\s+".toRegex(), " ").trim() == pattern
        }
    }
    
    private fun generateVerbalSummary(detections: List<PatternMatch>): VoiceCommandResult {
        val summary = when {
            detections.isEmpty() -> "No patterns detected"
            detections.size == 1 -> {
                val det = detections.first()
                "Detected one ${det.patternName} pattern with ${(det.confidence * 100).toInt()}% confidence"
            }
            else -> {
                val byPattern = detections.groupBy { it.patternName }
                val topPatterns = byPattern.entries
                    .sortedByDescending { it.value.size }
                    .take(3)
                    .joinToString(", ") { "${it.value.size} ${it.key}" }
                "Detected ${detections.size} patterns: $topPatterns"
            }
        }
        return VoiceCommandResult.VerbalSummary(summary)
    }
    
    private fun exportAllDetections(context: Context) {
        val sessionDir = File(context.filesDir, "current_session").apply { mkdirs() }
        NotebookExporter.export(context, sessionDir)
    }
    
    fun getAllCommands(): List<String> {
        return listOf(
            "Show bull flags - Filter and display bull flag patterns",
            "Show bear flags - Filter and display bear flag patterns",
            "Show all patterns - Clear all filters",
            "Export all detections - Generate PDF report",
            "Export to PDF - Generate PDF report",
            "Start scanning - Begin pattern detection",
            "Stop scanning - Pause detection",
            "What did you detect? - Get verbal summary",
            "Show high confidence - Filter patterns >75%",
            "Show recent patterns - Show last 5 minutes",
            "Clear highlights - Remove all highlights",
            "Refresh detection - Re-scan patterns",
            "Show achievements - Open achievements",
            "Show analytics - Open pattern analytics",
            "Show predictions - Open predictions",
            "Show learning - Open learning dashboard",
            "Show advanced learning - Open advanced learning analytics",
            "Open export - Open export center",
            "Show performance - Open performance dashboard",
            "Show about - Open app information",
            "Help - Show this list"
        )
    }
    
    fun getCommandFeedback(result: VoiceCommandResult): String {
        return when (result) {
            is VoiceCommandResult.FilterPattern -> 
                "Filtering to show ${result.patternType.replace("_", " ")} patterns"
            is VoiceCommandResult.ClearFilter -> 
                "Showing all patterns"
            is VoiceCommandResult.ExportPDF -> 
                "Exporting detections to PDF"
            is VoiceCommandResult.StartScanning -> 
                "Starting pattern detection"
            is VoiceCommandResult.StopScanning -> 
                "Stopping pattern detection"
            is VoiceCommandResult.FilterByConfidence -> 
                "Showing patterns with confidence above ${(result.threshold * 100).toInt()}%"
            is VoiceCommandResult.FilterByTime -> 
                "Showing patterns from last ${result.minutesAgo} minutes"
            is VoiceCommandResult.ClearHighlights -> 
                "Clearing all highlights"
            is VoiceCommandResult.RefreshDetection -> 
                "Refreshing pattern detection"
            is VoiceCommandResult.NavigateAchievements -> 
                "Opening achievements"
            is VoiceCommandResult.NavigateAnalytics -> 
                "Opening pattern analytics"
            is VoiceCommandResult.NavigatePredictions -> 
                "Opening predictions"
            is VoiceCommandResult.NavigateLearning -> 
                "Opening learning dashboard"
            is VoiceCommandResult.NavigateAdvancedLearning -> 
                "Opening advanced learning analytics"
            is VoiceCommandResult.NavigateExport -> 
                "Opening export center"
            is VoiceCommandResult.NavigatePerformance -> 
                "Opening performance dashboard"
            is VoiceCommandResult.NavigateAbout -> 
                "Opening app information"
            is VoiceCommandResult.VerbalSummary -> 
                result.summary
            is VoiceCommandResult.ShowHelp -> 
                "Available commands: ${result.commands.joinToString("; ")}"
            is VoiceCommandResult.CommandNotRecognized -> 
                "Command not recognized. Say 'help' for available commands"
        }
    }
}
