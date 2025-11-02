package com.lamontlabs.quantravision.export

import android.content.Context
import com.lamontlabs.quantravision.export.model.PatternReport
import java.io.File

/**
 * Abstract base class for report generators.
 * Implementations: PDF and CSV formats.
 */
abstract class ReportGenerator {
    
    /**
     * Generate a report file from pattern data.
     * 
     * @param context Android context
     * @param report Pattern report data
     * @return Generated file
     */
    abstract fun generate(context: Context, report: PatternReport): File
    
    /**
     * Get the file extension for this report format.
     */
    abstract fun getExtension(): String
    
    /**
     * Get the MIME type for sharing.
     */
    abstract fun getMimeType(): String
    
    /**
     * Validate report data before generation.
     */
    protected fun validate(report: PatternReport) {
        require(report.patterns.isNotEmpty()) {
            "Cannot generate empty report"
        }
        require(report.metadata.title.isNotBlank()) {
            "Report title cannot be blank"
        }
    }
    
    /**
     * Create output directory for reports.
     */
    protected fun getReportDirectory(context: Context): File {
        return File(context.filesDir, "reports").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    /**
     * Generate filename with timestamp.
     */
    protected fun generateFilename(prefix: String, extension: String): String {
        val timestamp = java.text.SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            java.util.Locale.US
        ).format(java.util.Date())
        return "${prefix}_${timestamp}.${extension}"
    }
    
    /**
     * Format disclaimer text for inclusion in reports.
     */
    protected fun formatDisclaimers(disclaimers: List<String>): String {
        return buildString {
            appendLine("IMPORTANT DISCLAIMERS:")
            appendLine()
            disclaimers.forEachIndexed { index, disclaimer ->
                appendLine("${index + 1}. $disclaimer")
            }
        }
    }
    
    /**
     * Get app version for report metadata.
     */
    protected fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    companion object {
        const val REPORT_PREFIX = "quantravision_report"
    }
}
