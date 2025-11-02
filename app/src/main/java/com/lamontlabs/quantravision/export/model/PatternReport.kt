package com.lamontlabs.quantravision.export.model

import com.lamontlabs.quantravision.PatternMatch
import java.util.Date

/**
 * Data model for pattern detection reports.
 * Supports both PDF and CSV export formats.
 */
data class PatternReport(
    val patterns: List<PatternMatch>,
    val metadata: ReportMetadata,
    val timestamp: Long = System.currentTimeMillis(),
    val disclaimers: List<String> = defaultDisclaimers()
) {
    
    data class ReportMetadata(
        val title: String = "QuantraVision Pattern Detection Report",
        val version: String = "1.0",
        val generatedBy: String = "QuantraVision",
        val dateRange: DateRange? = null,
        val filterCriteria: FilterCriteria? = null,
        val statistics: ReportStatistics? = null
    )
    
    data class DateRange(
        val startDate: Long,
        val endDate: Long
    ) {
        fun describe(): String {
            val start = Date(startDate)
            val end = Date(endDate)
            return "${start.toLocaleString()} - ${end.toLocaleString()}"
        }
    }
    
    data class FilterCriteria(
        val patternTypes: List<String>? = null,
        val minConfidence: Double? = null,
        val timeframes: List<String>? = null
    ) {
        fun describe(): String {
            val parts = mutableListOf<String>()
            patternTypes?.let { parts.add("Patterns: ${it.joinToString(", ")}") }
            minConfidence?.let { parts.add("Min Confidence: ${(it * 100).toInt()}%") }
            timeframes?.let { parts.add("Timeframes: ${it.joinToString(", ")}") }
            return parts.joinToString(" | ")
        }
    }
    
    data class ReportStatistics(
        val totalPatterns: Int,
        val uniquePatternTypes: Int,
        val averageConfidence: Double,
        val highestConfidence: Double,
        val lowestConfidence: Double,
        val patternsByTimeframe: Map<String, Int>,
        val topPatterns: List<Pair<String, Int>>
    )
    
    enum class ExportFormat {
        PDF,
        CSV
    }
    
    companion object {
        fun defaultDisclaimers(): List<String> = listOf(
            "NOT FINANCIAL ADVICE: This report is for educational purposes only.",
            "Pattern detection does not guarantee future price movements.",
            "Trading involves substantial risk of loss and is not suitable for all investors.",
            "Past performance is not indicative of future results.",
            "Always conduct your own research and consult with a licensed financial advisor.",
            "QuantraVision is a pattern recognition tool, not a trading signal generator."
        )
        
        fun create(
            patterns: List<PatternMatch>,
            title: String = "QuantraVision Pattern Detection Report",
            dateRange: DateRange? = null,
            filterCriteria: FilterCriteria? = null
        ): PatternReport {
            // Calculate statistics
            val stats = if (patterns.isNotEmpty()) {
                ReportStatistics(
                    totalPatterns = patterns.size,
                    uniquePatternTypes = patterns.map { it.patternName }.toSet().size,
                    averageConfidence = patterns.map { it.confidence }.average(),
                    highestConfidence = patterns.maxOf { it.confidence },
                    lowestConfidence = patterns.minOf { it.confidence },
                    patternsByTimeframe = patterns.groupBy { it.timeframe }
                        .mapValues { it.value.size },
                    topPatterns = patterns.groupBy { it.patternName }
                        .mapValues { it.value.size }
                        .entries
                        .sortedByDescending { it.value }
                        .take(5)
                        .map { it.key to it.value }
                )
            } else {
                null
            }
            
            val metadata = ReportMetadata(
                title = title,
                dateRange = dateRange,
                filterCriteria = filterCriteria,
                statistics = stats
            )
            
            return PatternReport(
                patterns = patterns,
                metadata = metadata
            )
        }
    }
}
