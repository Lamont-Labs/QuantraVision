package com.lamontlabs.quantravision.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.lamontlabs.quantravision.PatternMatch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * PDFReportGenerator
 * Generates professional PDF reports of pattern detections
 * Watermark-free for Pro users, branded for all tiers
 */
object PDFReportGenerator {

    data class ReportConfig(
        val title: String = "QuantraVision Pattern Detection Report",
        val includeCharts: Boolean = true,
        val includeStatistics: Boolean = true,
        val watermark: Boolean = false,
        val theme: String = "dark" // "dark" or "light"
    )

    fun generate(
        context: Context,
        matches: List<PatternMatch>,
        config: ReportConfig = ReportConfig()
    ): File {
        val document = PdfDocument()
        val pageWidth = 612 // 8.5 inches at 72 DPI
        val pageHeight = 792 // 11 inches at 72 DPI

        // Page 1: Title and Summary
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        
        drawTitlePage(page.canvas, config, matches.size)
        document.finishPage(page)

        // Page 2: Statistics
        if (config.includeStatistics && matches.isNotEmpty()) {
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            drawStatisticsPage(page.canvas, matches)
            document.finishPage(page)
        }

        // Subsequent pages: Individual detections
        matches.chunked(3).forEach { chunk ->
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            drawDetectionsPage(page.canvas, chunk, config)
            document.finishPage(page)
        }

        // Save to file
        val outDir = File(context.filesDir, "reports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outFile = File(outDir, "quantravision_report_$timestamp.pdf")

        FileOutputStream(outFile).use { output ->
            document.writeTo(output)
        }
        document.close()

        return outFile
    }

    private fun drawTitlePage(canvas: Canvas, config: ReportConfig, detectionCount: Int) {
        val paint = Paint().apply {
            color = if (config.theme == "dark") Color.WHITE else Color.BLACK
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Background
        canvas.drawColor(if (config.theme == "dark") Color.parseColor("#0A1218") else Color.WHITE)

        // Title
        canvas.drawText(config.title, 50f, 100f, paint)

        // Subtitle
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Lamont Labs", 50f, 140f, paint)

        // Date
        val dateStr = SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.US).format(Date())
        canvas.drawText("Generated: $dateStr", 50f, 180f, paint)

        // Summary box
        paint.color = Color.parseColor("#00E5FF")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRect(50f, 250f, 562f, 400f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 24f
        canvas.drawText("Detection Summary", 70f, 290f, paint)

        paint.color = if (config.theme == "dark") Color.WHITE else Color.BLACK
        paint.textSize = 16f
        canvas.drawText("Total Patterns Detected: $detectionCount", 70f, 330f, paint)
        canvas.drawText("Confidence Range: ${if (detectionCount > 0) "Check Statistics" else "N/A"}", 70f, 360f, paint)

        // Watermark
        if (config.watermark) {
            paint.color = Color.parseColor("#66000000")
            paint.textSize = 48f
            paint.alpha = 50
            canvas.save()
            canvas.rotate(-45f, 306f, 396f)
            canvas.drawText("FREE TIER", 180f, 400f, paint)
            canvas.restore()
        }

        // Footer
        paint.color = Color.GRAY
        paint.textSize = 12f
        paint.alpha = 255
        canvas.drawText("QuantraVision • Offline AI Pattern Detection", 50f, 750f, paint)
        canvas.drawText("Not Financial Advice • Educational Use Only", 50f, 770f, paint)
    }

    private fun drawStatisticsPage(canvas: Canvas, matches: List<PatternMatch>) {
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawColor(Color.parseColor("#0A1218"))

        // Title
        paint.textSize = 24f
        canvas.drawText("Detection Statistics", 50f, 80f, paint)

        // Calculate statistics
        val avgConfidence = matches.map { it.confidence }.average()
        val maxConfidence = matches.maxOfOrNull { it.confidence } ?: 0.0
        val minConfidence = matches.minOfOrNull { it.confidence } ?: 0.0
        val uniquePatterns = matches.map { it.patternName }.toSet().size
        val byTimeframe = matches.groupBy { it.timeframe }

        var y = 130f
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        canvas.drawText("Average Confidence: ${"%.2f".format(avgConfidence * 100)}%", 70f, y, paint)
        y += 30f
        canvas.drawText("Highest Confidence: ${"%.2f".format(maxConfidence * 100)}%", 70f, y, paint)
        y += 30f
        canvas.drawText("Lowest Confidence: ${"%.2f".format(minConfidence * 100)}%", 70f, y, paint)
        y += 30f
        canvas.drawText("Unique Pattern Types: $uniquePatterns", 70f, y, paint)
        y += 50f

        // Timeframe breakdown
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Detections by Timeframe:", 70f, y, paint)
        y += 30f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        byTimeframe.forEach { (timeframe, tfMatches) ->
            canvas.drawText("$timeframe: ${tfMatches.size} detections", 90f, y, paint)
            y += 25f
        }

        // Top patterns
        y += 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Most Detected Patterns:", 70f, y, paint)
        y += 30f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        matches.groupBy { it.patternName }
            .entries
            .sortedByDescending { it.value.size }
            .take(5)
            .forEach { (pattern, patternMatches) ->
                canvas.drawText("${pattern}: ${patternMatches.size} occurrences", 90f, y, paint)
                y += 25f
            }
    }

    private fun drawDetectionsPage(canvas: Canvas, matches: List<PatternMatch>, config: ReportConfig) {
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 14f
            isAntiAlias = true
        }

        canvas.drawColor(Color.parseColor("#0A1218"))

        var y = 50f
        matches.forEach { match ->
            // Pattern box
            paint.color = Color.parseColor("#00E5FF")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            canvas.drawRect(50f, y, 562f, y + 180f, paint)

            // Pattern info
            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(match.patternName, 70f, y + 30f, paint)

            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Confidence: ${"%.2f".format(match.confidence * 100)}%", 70f, y + 60f, paint)
            canvas.drawText("Timeframe: ${match.timeframe}", 70f, y + 85f, paint)
            canvas.drawText("Scale: ${"%.2f".format(match.scale)}", 70f, y + 110f, paint)
            
            val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(match.timestamp))
            canvas.drawText("Detected: $dateStr", 70f, y + 135f, paint)

            // Confidence bar
            paint.color = Color.parseColor("#00E5FF")
            paint.style = Paint.Style.FILL
            val barWidth = (match.confidence * 400).toFloat()
            canvas.drawRect(70f, y + 150f, 70f + barWidth, y + 165f, paint)

            y += 200f
        }
    }
}
