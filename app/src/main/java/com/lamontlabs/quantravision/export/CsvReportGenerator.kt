package com.lamontlabs.quantravision.export

import android.content.Context
import com.lamontlabs.quantravision.export.model.PatternReport
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * CSV report generator for pattern detections.
 * Generates properly escaped CSV files with disclaimers.
 */
object CsvReportGenerator : ReportGenerator() {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    
    override fun generate(context: Context, report: PatternReport): File {
        validate(report)
        
        val reportDir = getReportDirectory(context)
        val filename = generateFilename(REPORT_PREFIX, getExtension())
        val file = File(reportDir, filename)
        
        FileWriter(file).use { writer ->
            // Write header with disclaimers
            writeDisclaimers(writer, report.disclaimers)
            writer.write("\n")
            
            // Write metadata
            writeMetadata(writer, report, context)
            writer.write("\n")
            
            // Write CSV header
            writeHeader(writer)
            
            // Write pattern data
            report.patterns.forEach { pattern ->
                writePattern(writer, pattern)
            }
            
            // Write statistics footer
            report.metadata.statistics?.let { stats ->
                writer.write("\n")
                writeStatistics(writer, stats)
            }
        }
        
        Timber.i("CSV report generated: ${file.absolutePath}")
        return file
    }
    
    override fun getExtension(): String = "csv"
    
    override fun getMimeType(): String = "text/csv"
    
    private fun writeDisclaimers(writer: FileWriter, disclaimers: List<String>) {
        writer.write("# QUANTRAVISION PATTERN DETECTION REPORT\n")
        writer.write("# ${formatDisclaimers(disclaimers).replace("\n", "\n# ")}")
    }
    
    private fun writeMetadata(writer: FileWriter, report: PatternReport, context: Context) {
        writer.write("# Report Title: ${report.metadata.title}\n")
        writer.write("# Generated: ${dateFormat.format(Date(report.timestamp))}\n")
        writer.write("# Version: ${report.metadata.version}\n")
        writer.write("# App Version: ${getAppVersion(context)}\n")
        
        report.metadata.dateRange?.let {
            writer.write("# Date Range: ${it.describe()}\n")
        }
        
        report.metadata.filterCriteria?.let {
            if (it.describe().isNotBlank()) {
                writer.write("# Filters: ${it.describe()}\n")
            }
        }
    }
    
    private fun writeHeader(writer: FileWriter) {
        writer.write(
            "Timestamp,Pattern Name,Confidence (%),Timeframe," +
            "Scale,Consensus Score,Detection Bounds,Origin,Status\n"
        )
    }
    
    private fun writePattern(writer: FileWriter, pattern: com.lamontlabs.quantravision.PatternMatch) {
        val row = listOf(
            dateFormat.format(Date(pattern.timestamp)),
            pattern.patternName,
            String.format("%.2f", pattern.confidence * 100),
            pattern.timeframe,
            String.format("%.2f", pattern.scale),
            String.format("%.3f", pattern.consensusScore),
            pattern.detectionBounds ?: "N/A",
            pattern.originPath,
            determineStatus(pattern.confidence)
        )
        
        // Escape and write CSV row
        writer.write(row.joinToString(",") { escapeCSV(it) })
        writer.write("\n")
    }
    
    private fun writeStatistics(writer: FileWriter, stats: PatternReport.ReportStatistics) {
        writer.write("\n# SUMMARY STATISTICS\n")
        writer.write("# Total Patterns: ${stats.totalPatterns}\n")
        writer.write("# Unique Pattern Types: ${stats.uniquePatternTypes}\n")
        writer.write("# Average Confidence: ${String.format("%.2f", stats.averageConfidence * 100)}%\n")
        writer.write("# Highest Confidence: ${String.format("%.2f", stats.highestConfidence * 100)}%\n")
        writer.write("# Lowest Confidence: ${String.format("%.2f", stats.lowestConfidence * 100)}%\n")
        
        writer.write("\n# TOP PATTERNS\n")
        stats.topPatterns.forEach { (pattern, count) ->
            writer.write("# $pattern: $count occurrences\n")
        }
    }
    
    private fun determineStatus(confidence: Double): String {
        return when {
            confidence >= 0.7 -> "High Confidence"
            confidence >= 0.5 -> "Medium Confidence"
            else -> "Low Confidence"
        }
    }
    
    /**
     * Escape CSV values according to RFC 4180.
     * Fields containing comma, quote, or newline must be quoted.
     * Quotes within fields must be doubled.
     */
    private fun escapeCSV(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }
}
