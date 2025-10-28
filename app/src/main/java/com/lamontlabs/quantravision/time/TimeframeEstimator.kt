package com.lamontlabs.quantravision.time

import android.graphics.Bitmap
import android.graphics.Color
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * TimeframeEstimator
 * Advanced chart timeframe estimation using computer vision
 * Analyzes: candle widths, grid patterns, periodicity, bar spacing
 */
object TimeframeEstimator {

    enum class Timeframe(val label: String, val minutes: Int) {
        M1("1m", 1),
        M5("5m", 5),
        M15("15m", 15),
        M30("30m", 30),
        H1("1h", 60),
        H4("4h", 240),
        D1("1d", 1440),
        W1("1w", 10080),
        MN1("1M", 43200)
    }

    /**
     * Advanced timeframe estimation using multiple CV techniques
     */
    fun estimateFromBitmap(bitmap: Bitmap): TimeframeEstimate {
        try {
            // Convert to OpenCV Mat for advanced analysis
            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)
            
            // Convert to grayscale for processing
            val gray = Mat()
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY)
            
            // Run multiple analysis techniques
            val candleWidth = analyzeCandleWidth(gray)
            val gridSpacing = analyzeGridSpacing(gray)
            val barDensity = analyzeBarDensity(gray)
            val periodicity = analyzePeriodicityFFT(gray)
            val volatilityPattern = analyzeVolatilityPattern(bitmap)
            
            // Combine all signals
            val timeframe = estimateFromAdvancedSignals(
                candleWidth = candleWidth,
                gridSpacing = gridSpacing,
                barDensity = barDensity,
                periodicity = periodicity,
                volatility = volatilityPattern,
                imageWidth = bitmap.width
            )
            
            // Calculate confidence from signal agreement
            val confidence = calculateAdvancedConfidence(
                candleWidth, gridSpacing, barDensity, periodicity
            )
            
            // Cleanup
            mat.release()
            gray.release()
            
            return TimeframeEstimate(
                timeframe = timeframe,
                confidence = confidence,
                candleWidth = candleWidth,
                gridSpacing = gridSpacing,
                barDensity = barDensity,
                periodicityScore = periodicity,
                volatilityScore = volatilityPattern
            )
        } catch (e: Exception) {
            // Fallback to basic estimation
            return estimateBasic(bitmap)
        }
    }

    /**
     * Analyze actual candlestick/bar widths
     * Narrower candles = shorter timeframe (more data points)
     */
    private fun analyzeCandleWidth(gray: Mat): Double {
        val height = gray.rows()
        val width = gray.cols()
        val sampleRow = height / 2
        
        // Detect edges to find candle boundaries
        val edges = Mat()
        Imgproc.Canny(gray, edges, 50.0, 150.0)
        
        val edgePositions = mutableListOf<Int>()
        for (x in 0 until width) {
            if (edges.get(sampleRow, x)[0] > 0) {
                edgePositions.add(x)
            }
        }
        
        // Calculate average spacing between edges (candle width)
        if (edgePositions.size < 2) {
            edges.release()
            return 0.5
        }
        
        val spacings = mutableListOf<Int>()
        for (i in 1 until edgePositions.size) {
            val spacing = edgePositions[i] - edgePositions[i-1]
            if (spacing > 3) { // Filter noise
                spacings.add(spacing)
            }
        }
        
        edges.release()
        
        if (spacings.isEmpty()) return 0.5
        
        val avgSpacing = spacings.average()
        // Normalize: smaller spacing = shorter timeframe
        return (avgSpacing / width).coerceIn(0.01, 0.5)
    }

    /**
     * Detect and analyze grid line spacing
     * Grid spacing correlates with time intervals
     */
    private fun analyzeGridSpacing(gray: Mat): Double {
        val height = gray.rows()
        val width = gray.cols()
        
        // Use Hough Line Transform to detect vertical grid lines
        val edges = Mat()
        Imgproc.Canny(gray, edges, 30.0, 90.0)
        
        val lines = Mat()
        Imgproc.HoughLinesP(edges, lines, 1.0, Math.PI / 180, 50, 30.0, 10.0)
        
        // Extract vertical line positions
        val verticalLines = mutableListOf<Int>()
        for (i in 0 until lines.rows()) {
            val line = lines.get(i, 0)
            val x1 = line[0].toInt()
            val x2 = line[2].toInt()
            val y1 = line[1].toInt()
            val y2 = line[3].toInt()
            
            // Check if line is mostly vertical (grid line)
            if (abs(x2 - x1) < 5 && abs(y2 - y1) > height / 3) {
                verticalLines.add((x1 + x2) / 2)
            }
        }
        
        edges.release()
        lines.release()
        
        if (verticalLines.size < 2) return 0.5
        
        // Calculate average spacing between grid lines
        verticalLines.sort()
        val spacings = mutableListOf<Int>()
        for (i in 1 until verticalLines.size) {
            spacings.add(verticalLines[i] - verticalLines[i-1])
        }
        
        if (spacings.isEmpty()) return 0.5
        
        val avgGridSpacing = spacings.average()
        return (avgGridSpacing / width).coerceIn(0.05, 0.5)
    }

    /**
     * Analyze bar density using advanced edge detection
     */
    private fun analyzeBarDensity(gray: Mat): Double {
        val width = gray.cols()
        val height = gray.rows()
        val sampleRow = height / 2
        
        var transitionCount = 0
        var lastIntensity = gray.get(sampleRow, 0)[0]
        
        for (x in 1 until width step 2) {
            val intensity = gray.get(sampleRow, x)[0]
            if (abs(intensity - lastIntensity) > 20.0) {
                transitionCount++
            }
            lastIntensity = intensity
        }
        
        return transitionCount.toDouble() / (width / 2.0)
    }

    /**
     * Analyze periodicity using frequency domain analysis
     * Shorter timeframes have higher frequency components
     */
    private fun analyzePeriodicityFFT(gray: Mat): Double {
        val width = gray.cols()
        val height = gray.rows()
        val sampleRow = height / 2
        
        // Extract horizontal scanline
        val scanline = DoubleArray(width)
        for (x in 0 until width) {
            scanline[x] = gray.get(sampleRow, x)[0]
        }
        
        // Simple frequency analysis: count oscillations
        var oscillations = 0
        var increasing = scanline[1] > scanline[0]
        
        for (i in 2 until scanline.size) {
            val nowIncreasing = scanline[i] > scanline[i-1]
            if (nowIncreasing != increasing) {
                oscillations++
                increasing = nowIncreasing
            }
        }
        
        // Normalize: more oscillations = shorter timeframe
        return oscillations.toDouble() / width
    }

    /**
     * Analyze volatility patterns using variance analysis
     */
    private fun analyzeVolatilityPattern(bitmap: Bitmap): Double {
        val width = bitmap.width
        val height = bitmap.height
        
        var totalVariance = 0.0
        val sampleColumns = 20
        
        for (i in 0 until sampleColumns) {
            val x = (width * i) / sampleColumns
            val intensities = mutableListOf<Int>()
            
            for (y in 0 until height step 3) {
                val pixel = bitmap.getPixel(x, y)
                val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                intensities.add(brightness)
            }
            
            // Calculate variance for this column
            val mean = intensities.average()
            val variance = intensities.map { (it - mean) * (it - mean) }.average()
            totalVariance += sqrt(variance)
        }
        
        return (totalVariance / sampleColumns / 255.0).coerceIn(0.0, 1.0)
    }

    /**
     * Combine advanced signals with weighted scoring
     */
    private fun estimateFromAdvancedSignals(
        candleWidth: Double,
        gridSpacing: Double,
        barDensity: Double,
        periodicity: Double,
        volatility: Double,
        imageWidth: Int
    ): Timeframe {
        // Weight each signal based on reliability
        val candleScore = when {
            candleWidth < 0.02 -> -3  // Very narrow = very short TF
            candleWidth < 0.05 -> -2
            candleWidth < 0.10 -> -1
            candleWidth < 0.15 -> 0
            candleWidth < 0.25 -> 1
            else -> 2  // Wide candles = longer TF
        }
        
        val gridScore = when {
            gridSpacing < 0.08 -> -2  // Tight grid = short TF
            gridSpacing < 0.15 -> -1
            gridSpacing < 0.25 -> 0
            else -> 2  // Wide grid = long TF
        }
        
        val densityScore = when {
            barDensity > 0.8 -> -3
            barDensity > 0.5 -> -1
            barDensity > 0.3 -> 0
            barDensity > 0.15 -> 1
            else -> 3
        }
        
        val periodicityScore = when {
            periodicity > 0.5 -> -2  // High frequency = short TF
            periodicity > 0.3 -> -1
            periodicity > 0.15 -> 0
            else -> 2  // Low frequency = long TF
        }
        
        val volatilityScore = when {
            volatility > 0.6 -> -1
            volatility > 0.4 -> 0
            else -> 1
        }
        
        // Weighted combination (candle width most reliable)
        val totalScore = (candleScore * 2) + gridScore + densityScore + periodicityScore + volatilityScore
        
        return when {
            totalScore < -6 -> Timeframe.M1
            totalScore < -3 -> Timeframe.M5
            totalScore < -1 -> Timeframe.M15
            totalScore < 2 -> Timeframe.H1
            totalScore < 4 -> Timeframe.H4
            totalScore < 7 -> Timeframe.D1
            else -> Timeframe.W1
        }
    }

    /**
     * Calculate confidence from signal agreement
     */
    private fun calculateAdvancedConfidence(
        candleWidth: Double,
        gridSpacing: Double,
        barDensity: Double,
        periodicity: Double
    ): Double {
        // Strong signals have clear values (not mid-range)
        val candleConfidence = when {
            candleWidth < 0.05 || candleWidth > 0.20 -> 0.9
            candleWidth < 0.08 || candleWidth > 0.15 -> 0.7
            else -> 0.5
        }
        
        val gridConfidence = when {
            gridSpacing < 0.10 || gridSpacing > 0.20 -> 0.8
            else -> 0.6
        }
        
        val densityConfidence = when {
            barDensity > 0.6 || barDensity < 0.25 -> 0.8
            else -> 0.6
        }
        
        val periodicityConfidence = when {
            periodicity > 0.4 || periodicity < 0.15 -> 0.7
            else -> 0.5
        }
        
        // Weighted average favoring most reliable signals
        val weighted = (candleConfidence * 0.35 +
                       gridConfidence * 0.25 +
                       densityConfidence * 0.25 +
                       periodicityConfidence * 0.15)
        
        return weighted.coerceIn(0.6, 0.95)
    }

    /**
     * Basic estimation fallback
     */
    private fun estimateBasic(bitmap: Bitmap): TimeframeEstimate {
        val width = bitmap.width
        
        val timeframe = when {
            width < 800 -> Timeframe.M5
            width < 1200 -> Timeframe.M15
            width < 1600 -> Timeframe.H1
            width < 2000 -> Timeframe.H4
            else -> Timeframe.D1
        }
        
        return TimeframeEstimate(
            timeframe = timeframe,
            confidence = 0.6,
            candleWidth = 0.0,
            gridSpacing = 0.0,
            barDensity = 0.0,
            periodicityScore = 0.0,
            volatilityScore = 0.0
        )
    }

    /**
     * Estimate from filename patterns
     */
    fun estimateFromFilename(filename: String): TimeframeEstimate {
        val lower = filename.lowercase()
        
        val timeframe = when {
            lower.contains("1m") || lower.contains("m1") -> Timeframe.M1
            lower.contains("5m") || lower.contains("m5") -> Timeframe.M5
            lower.contains("15m") || lower.contains("m15") -> Timeframe.M15
            lower.contains("30m") || lower.contains("m30") -> Timeframe.M30
            lower.contains("1h") || lower.contains("h1") -> Timeframe.H1
            lower.contains("4h") || lower.contains("h4") -> Timeframe.H4
            lower.contains("1d") || lower.contains("d1") || lower.contains("daily") -> Timeframe.D1
            lower.contains("1w") || lower.contains("w1") || lower.contains("weekly") -> Timeframe.W1
            lower.contains("1m") || lower.contains("monthly") -> Timeframe.MN1
            else -> Timeframe.H1
        }
        
        return TimeframeEstimate(
            timeframe = timeframe,
            confidence = if (timeframe == Timeframe.H1) 0.5 else 0.95,
            candleWidth = 0.0,
            gridSpacing = 0.0,
            barDensity = 0.0,
            periodicityScore = 0.0,
            volatilityScore = 0.0
        )
    }

    /**
     * Combined estimation using both CV analysis and filename
     */
    fun estimateCombined(bitmap: Bitmap, filename: String): TimeframeEstimate {
        val imageEstimate = estimateFromBitmap(bitmap)
        val filenameEstimate = estimateFromFilename(filename)
        
        // Filename with explicit label wins
        if (filenameEstimate.confidence > 0.9) {
            return filenameEstimate
        }
        
        // Strong image analysis wins
        if (imageEstimate.confidence > 0.8) {
            return imageEstimate
        }
        
        // If both moderate, prefer image analysis
        return if (imageEstimate.confidence > 0.6) {
            imageEstimate
        } else {
            filenameEstimate.copy(confidence = 0.65)
        }
    }

    data class TimeframeEstimate(
        val timeframe: Timeframe,
        val confidence: Double,
        val candleWidth: Double = 0.0,
        val gridSpacing: Double = 0.0,
        val barDensity: Double = 0.0,
        val periodicityScore: Double = 0.0,
        val volatilityScore: Double = 0.0
    )
}
