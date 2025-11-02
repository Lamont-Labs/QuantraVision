package com.lamontlabs.quantravision.learning.advanced

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ReportGenerator(private val context: Context) {
    
    private val correlationAnalyzer = PatternCorrelationAnalyzer(context)
    private val marketConditionLearner = MarketConditionLearner(context)
    private val temporalLearner = TemporalPatternLearner(context)
    private val riskAnalyzer = RiskAdjustedAnalyzer(context)
    private val behavioralAnalyzer = BehavioralAnalyzer(context)
    private val strategyLearner = StrategyLearner(context)
    private val trendForecaster = TrendForecaster(context)
    private val anomalyDetector = AnomalyDetector(context)
    private val calibrator = GradientDescentCalibrator(context)
    
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 50
    
    suspend fun generateWeeklyReport(): File = withContext(Dispatchers.IO) {
        try {
            val reportFile = createReportFile("weekly")
            val document = PdfDocument()
            
            var pageNumber = 1
            
            pageNumber = addCoverPage(document, pageNumber, "Weekly Learning Report", 7)
            pageNumber = addExecutiveSummary(document, pageNumber)
            pageNumber = addPerformanceOverview(document, pageNumber)
            pageNumber = addBehavioralInsights(document, pageNumber)
            pageNumber = addTopRecommendations(document, pageNumber)
            
            document.writeTo(FileOutputStream(reportFile))
            document.close()
            
            Timber.i("Weekly report generated: ${reportFile.absolutePath}")
            reportFile
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate weekly report")
            throw e
        }
    }
    
    suspend fun generateMonthlyReport(): File = withContext(Dispatchers.IO) {
        try {
            val reportFile = createReportFile("monthly")
            val document = PdfDocument()
            
            var pageNumber = 1
            
            pageNumber = addCoverPage(document, pageNumber, "Monthly Learning Report", 30)
            pageNumber = addExecutiveSummary(document, pageNumber)
            pageNumber = addPerformanceOverview(document, pageNumber)
            pageNumber = addPatternAnalysis(document, pageNumber)
            pageNumber = addMarketConditionInsights(document, pageNumber)
            pageNumber = addTemporalInsights(document, pageNumber)
            pageNumber = addRiskAnalysis(document, pageNumber)
            pageNumber = addBehavioralInsights(document, pageNumber)
            pageNumber = addStrategyRecommendations(document, pageNumber)
            pageNumber = addPredictiveForecasts(document, pageNumber)
            pageNumber = addAnomaliesAndAlerts(document, pageNumber)
            
            document.writeTo(FileOutputStream(reportFile))
            document.close()
            
            Timber.i("Monthly report generated: ${reportFile.absolutePath}")
            reportFile
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate monthly report")
            throw e
        }
    }
    
    suspend fun generateProgressReport(): File = withContext(Dispatchers.IO) {
        try {
            val reportFile = createReportFile("progress")
            val document = PdfDocument()
            
            var pageNumber = 1
            
            pageNumber = addCoverPage(document, pageNumber, "All-Time Progress Report", -1)
            pageNumber = addExecutiveSummary(document, pageNumber)
            pageNumber = addPerformanceOverview(document, pageNumber)
            pageNumber = addPatternAnalysis(document, pageNumber)
            pageNumber = addRiskAnalysis(document, pageNumber)
            pageNumber = addStrategyRecommendations(document, pageNumber)
            
            document.writeTo(FileOutputStream(reportFile))
            document.close()
            
            Timber.i("Progress report generated: ${reportFile.absolutePath}")
            reportFile
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate progress report")
            throw e
        }
    }
    
    private fun createReportFile(reportType: String): File {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val fileName = "quantravision_${reportType}_report_$timestamp.pdf"
        val reportsDir = File(context.filesDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        return File(reportsDir, fileName)
    }
    
    private fun addCoverPage(
        document: PdfDocument,
        pageNumber: Int,
        title: String,
        daysCovered: Int
    ): Int {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        val canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 28f
        paint.isFakeBoldText = true
        canvas.drawText(title, margin.toFloat(), 200f, paint)
        
        paint.textSize = 16f
        paint.isFakeBoldText = false
        val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
        canvas.drawText(dateStr, margin.toFloat(), 250f, paint)
        
        if (daysCovered > 0) {
            canvas.drawText("Covering last $daysCovered days", margin.toFloat(), 280f, paint)
        } else {
            canvas.drawText("All-time statistics", margin.toFloat(), 280f, paint)
        }
        
        paint.textSize = 12f
        val disclaimer = "⚠️ EDUCATIONAL TOOL ONLY - NOT FINANCIAL ADVICE"
        canvas.drawText(disclaimer, margin.toFloat(), pageHeight - 100f, paint)
        canvas.drawText("Past performance does not predict future results", margin.toFloat(), pageHeight - 80f, paint)
        
        document.finishPage(page)
        return pageNumber + 1
    }
    
    private suspend fun addExecutiveSummary(document: PdfDocument, pageNumber: Int): Int {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        val canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Executive Summary", margin.toFloat(), 100f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        var y = 150f
        
        try {
            val topPatterns = riskAnalyzer.getBestRiskAdjusted().take(3)
            canvas.drawText("Top 3 Insights:", margin.toFloat(), y, paint)
            y += 30f
            
            topPatterns.forEachIndexed { index, pattern ->
                val text = "${index + 1}. ${pattern.patternType}: ${(pattern.winRate * 100).toInt()}% win rate, Sharpe ${String.format("%.2f", pattern.sharpeRatio)}"
                canvas.drawText(text, margin.toFloat() + 20, y, paint)
                y += 25f
            }
            
            val warnings = behavioralAnalyzer.getBehavioralWarnings()
            if (warnings.isNotEmpty()) {
                y += 20f
                canvas.drawText("Behavioral Alerts:", margin.toFloat(), y, paint)
                y += 30f
                
                warnings.take(2).forEach { warning ->
                    canvas.drawText("• ${warning.message}", margin.toFloat() + 20, y, paint)
                    y += 25f
                }
            }
        } catch (e: Exception) {
            canvas.drawText("Insufficient data for summary", margin.toFloat(), y, paint)
        }
        
        addDisclaimer(canvas, paint)
        document.finishPage(page)
        return pageNumber + 1
    }
    
    private suspend fun addPerformanceOverview(document: PdfDocument, pageNumber: Int): Int {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        val canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Performance Overview", margin.toFloat(), 100f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        var y = 150f
        
        try {
            val portfolio = strategyLearner.getPortfolioMetrics()
            
            canvas.drawText("Overall Statistics:", margin.toFloat(), y, paint)
            y += 30f
            canvas.drawText("  Total Patterns: ${portfolio.totalPatterns}", margin.toFloat() + 20, y, paint)
            y += 25f
            canvas.drawText("  Average Win Rate: ${(portfolio.avgWinRate * 100).toInt()}%", margin.toFloat() + 20, y, paint)
            y += 25f
            canvas.drawText("  Portfolio Sharpe Ratio: ${String.format("%.2f", portfolio.sharpeRatio)}", margin.toFloat() + 20, y, paint)
            y += 25f
            canvas.drawText("  Diversification Score: ${(portfolio.diversificationScore * 100).toInt()}%", margin.toFloat() + 20, y, paint)
        } catch (e: Exception) {
            canvas.drawText("Insufficient data for performance overview", margin.toFloat(), y, paint)
        }
        
        addDisclaimer(canvas, paint)
        document.finishPage(page)
        return pageNumber + 1
    }
    
    private suspend fun addPatternAnalysis(document: PdfDocument, pageNumber: Int): Int {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        val canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Pattern Analysis", margin.toFloat(), 100f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        var y = 150f
        
        try {
            val bestPatterns = riskAnalyzer.getBestRiskAdjusted().take(5)
            
            canvas.drawText("Best Performing Patterns:", margin.toFloat(), y, paint)
            y += 30f
            
            bestPatterns.forEach { pattern ->
                canvas.drawText("${pattern.patternType}: ${(pattern.winRate * 100).toInt()}% win rate (${pattern.sampleSize} trades)", 
                    margin.toFloat() + 20, y, paint)
                y += 25f
            }
        } catch (e: Exception) {
            canvas.drawText("Insufficient data for pattern analysis", margin.toFloat(), y, paint)
        }
        
        addDisclaimer(canvas, paint)
        document.finishPage(page)
        return pageNumber + 1
    }
    
    private fun addMarketConditionInsights(document: PdfDocument, pageNumber: Int): Int {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        val canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Market Condition Insights", margin.toFloat(), 100f, paint)
        
        paint.textSize = 12f
        canvas.drawText("Analysis by market conditions (requires more data)", margin.toFloat(), 150f, paint)
        
        addDisclaimer(canvas, paint)
        document.finishPage(page)
        return pageNumber + 1
    }
    
    private fun addTemporalInsights(document: PdfDocument, pageNumber: Int): Int {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        val canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Temporal Insights", margin.toFloat(), 100f, paint)
        
        paint.textSize = 12f
        canvas.drawText("Best times and days analysis (requires more data)", margin.toFloat(), 150f, paint)
        
        addDisclaimer(canvas, paint)
        document.finishPage(page)
        return pageNumber + 1
    }
    
    private suspend fun addRiskAnalysis(document: PdfDocument, pageNumber: Int): Int {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        val canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Risk Analysis", margin.toFloat(), 100f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        var y = 150f
        
        try {
            val bestRiskAdjusted = riskAnalyzer.getBestRiskAdjusted().take(5)
            
            canvas.drawText("Risk-Adjusted Performance:", margin.toFloat(), y, paint)
            y += 30f
            
            bestRiskAdjusted.forEach { pattern ->
                canvas.drawText("${pattern.patternType}: Sharpe ${String.format("%.2f", pattern.sharpeRatio)}, EV ${String.format("%.2f", pattern.expectedValue)}", 
                    margin.toFloat() + 20, y, paint)
                y += 25f
            }
        } catch (e: Exception) {
            canvas.drawText("Insufficient data for risk analysis", margin.toFloat(), y, paint)
        }
        
        addDisclaimer(canvas, paint)
        document.finishPage(page)
        return pageNumber + 1
    }
    
    private suspend fun addBehavioralInsights(document: PdfDocument, pageNumber: Int): Int {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        val canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Behavioral Insights", margin.toFloat(), 100f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        var y = 150f
        
        try {
            val warnings = behavioralAnalyzer.getBehavioralWarnings()
            
            canvas.drawText("Behavioral Patterns:", margin.toFloat(), y, paint)
            y += 30f
            
            if (warnings.isEmpty()) {
                canvas.drawText("No concerning behavioral patterns detected", margin.toFloat() + 20, y, paint)
            } else {
                warnings.forEach { warning ->
                    canvas.drawText("• ${warning.message}", margin.toFloat() + 20, y, paint)
                    y += 25f
                }
            }
        } catch (e: Exception) {
            canvas.drawText("Insufficient data for behavioral analysis", margin.toFloat(), y, paint)
        }
        
        addDisclaimer(canvas, paint)
        document.finishPage(page)
        return pageNumber + 1
    }
    
    private suspend fun addStrategyRecommendations(document: PdfDocument, pageNumber: Int): Int {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        val canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Strategy Recommendations", margin.toFloat(), 100f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        var y = 150f
        
        try {
            val portfolio = strategyLearner.getBestPortfolio(5)
            
            if (portfolio != null) {
                canvas.drawText("Recommended Portfolio:", margin.toFloat(), y, paint)
                y += 30f
                
                portfolio.patterns.forEach { pattern ->
                    val allocation = (portfolio.allocation[pattern] ?: 0.0f) * 100
                    canvas.drawText("${pattern}: ${allocation.toInt()}% allocation", margin.toFloat() + 20, y, paint)
                    y += 25f
                }
            } else {
                canvas.drawText("Insufficient data for portfolio recommendations", margin.toFloat(), y, paint)
            }
        } catch (e: Exception) {
            canvas.drawText("Insufficient data for strategy recommendations", margin.toFloat(), y, paint)
        }
        
        addDisclaimer(canvas, paint)
        document.finishPage(page)
        return pageNumber + 1
    }
    
    private fun addPredictiveForecasts(document: PdfDocument, pageNumber: Int): Int {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        val canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Predictive Forecasts", margin.toFloat(), 100f, paint)
        
        paint.textSize = 12f
        canvas.drawText("Trend forecasts (requires more historical data)", margin.toFloat(), 150f, paint)
        
        addDisclaimer(canvas, paint)
        document.finishPage(page)
        return pageNumber + 1
    }
    
    private suspend fun addAnomaliesAndAlerts(document: PdfDocument, pageNumber: Int): Int {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        val canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Anomalies & Alerts", margin.toFloat(), 100f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        var y = 150f
        
        try {
            val anomalies = anomalyDetector.detectAnomalies().take(5)
            
            if (anomalies.isEmpty()) {
                canvas.drawText("No anomalies detected", margin.toFloat(), y, paint)
            } else {
                canvas.drawText("Detected Anomalies:", margin.toFloat(), y, paint)
                y += 30f
                
                anomalies.forEach { anomaly ->
                    canvas.drawText("• ${anomaly.description}", margin.toFloat() + 20, y, paint)
                    y += 25f
                }
            }
        } catch (e: Exception) {
            canvas.drawText("Insufficient data for anomaly detection", margin.toFloat(), y, paint)
        }
        
        addDisclaimer(canvas, paint)
        document.finishPage(page)
        return pageNumber + 1
    }
    
    private fun addTopRecommendations(document: PdfDocument, pageNumber: Int): Int {
        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        val canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Top Recommendations", margin.toFloat(), 100f, paint)
        
        paint.textSize = 12f
        canvas.drawText("Focus on building pattern history for better insights", margin.toFloat(), 150f, paint)
        
        addDisclaimer(canvas, paint)
        document.finishPage(page)
        return pageNumber + 1
    }
    
    private fun addDisclaimer(canvas: android.graphics.Canvas, paint: Paint) {
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("⚠️ Educational tool only - Not financial advice", margin.toFloat(), pageHeight - 60f, paint)
        canvas.drawText("Past performance does not predict future results", margin.toFloat(), pageHeight - 45f, paint)
    }
}
