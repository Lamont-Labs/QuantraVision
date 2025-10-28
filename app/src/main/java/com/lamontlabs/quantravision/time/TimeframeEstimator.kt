package com.lamontlabs.quantravision.time

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

/**
 * TimeframeEstimator
 * Estimates the timeframe of a chart from actual image data analysis
 * Analyzes candle density, bar spacing, and visual patterns
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
     * Estimate timeframe from bitmap characteristics
     * Analyzes actual image data: bar density, spacing, volatility patterns
     */
    fun estimateFromBitmap(bitmap: Bitmap): TimeframeEstimate {
        val width = bitmap.width
        val height = bitmap.height
        
        // Analyze multiple image characteristics
        val barDensity = estimateBarDensity(bitmap)
        val volatilityPattern = estimateVolatilityPattern(bitmap)
        val aspectRatio = width.toDouble() / height.toDouble()
        
        // Combine signals for timeframe estimation
        val timeframe = estimateFromMultipleSignals(
            barDensity = barDensity,
            volatility = volatilityPattern,
            aspectRatio = aspectRatio,
            imageWidth = width
        )
        
        // Calculate confidence based on signal agreement
        val confidence = calculateConfidence(barDensity, volatilityPattern, aspectRatio)
        
        return TimeframeEstimate(
            timeframe = timeframe,
            confidence = confidence,
            barDensity = barDensity,
            volatilityScore = volatilityPattern
        )
    }

    /**
     * Analyze candle/bar density by detecting vertical structures
     * Higher density = shorter timeframe (more bars in view)
     */
    private fun estimateBarDensity(bitmap: Bitmap): Double {
        val width = bitmap.width
        val height = bitmap.height
        val sampleRow = height / 2 // Sample middle row
        
        var edgeCount = 0
        var lastBrightness = 0
        
        // Sample every 4 pixels for performance
        for (x in 0 until width step 4) {
            val pixel = bitmap.getPixel(x, sampleRow)
            val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
            
            // Detect edges (significant brightness changes indicate bar boundaries)
            if (abs(brightness - lastBrightness) > 30) {
                edgeCount++
            }
            lastBrightness = brightness
        }
        
        // Normalize by width
        return edgeCount.toDouble() / (width / 4.0)
    }

    /**
     * Analyze volatility patterns in the chart
     * Higher volatility typically indicates shorter timeframes
     */
    private fun estimateVolatilityPattern(bitmap: Bitmap): Double {
        val width = bitmap.width
        val height = bitmap.height
        
        // Sample vertical columns to detect price movement range
        var totalVariation = 0.0
        val sampleColumns = 10
        
        for (i in 0 until sampleColumns) {
            val x = (width * i) / sampleColumns
            var minBrightness = 255
            var maxBrightness = 0
            
            // Scan vertical column
            for (y in 0 until height step 2) {
                val pixel = bitmap.getPixel(x, y)
                val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                minBrightness = minOf(minBrightness, brightness)
                maxBrightness = maxOf(maxBrightness, brightness)
            }
            
            totalVariation += (maxBrightness - minBrightness).toDouble()
        }
        
        // Normalize
        return totalVariation / sampleColumns / 255.0
    }

    /**
     * Combine multiple signals to estimate timeframe
     */
    private fun estimateFromMultipleSignals(
        barDensity: Double,
        volatility: Double,
        aspectRatio: Double,
        imageWidth: Int
    ): Timeframe {
        // High density + high volatility = shorter timeframe
        val densityScore = when {
            barDensity > 0.8 -> -4 // Very high density = very short TF
            barDensity > 0.5 -> -2
            barDensity > 0.3 -> 0
            barDensity > 0.15 -> 2
            else -> 4 // Low density = longer TF
        }
        
        val volatilityScore = when {
            volatility > 0.6 -> -2 // High volatility = shorter TF
            volatility > 0.4 -> -1
            volatility > 0.2 -> 0
            else -> 2 // Low volatility = longer TF
        }
        
        val sizeScore = when {
            imageWidth < 800 -> -1
            imageWidth < 1600 -> 0
            imageWidth < 2400 -> 1
            else -> 2
        }
        
        val totalScore = densityScore + volatilityScore + sizeScore
        
        return when {
            totalScore < -4 -> Timeframe.M1
            totalScore < -2 -> Timeframe.M5
            totalScore < 0 -> Timeframe.M15
            totalScore < 2 -> Timeframe.H1
            totalScore < 4 -> Timeframe.H4
            totalScore < 6 -> Timeframe.D1
            else -> Timeframe.W1
        }
    }

    /**
     * Calculate confidence based on signal agreement
     */
    private fun calculateConfidence(
        barDensity: Double,
        volatility: Double,
        aspectRatio: Double
    ): Double {
        // If signals agree strongly, confidence is high
        val densityConfidence = when {
            barDensity > 0.7 || barDensity < 0.2 -> 0.9 // Clear signal
            barDensity > 0.5 || barDensity < 0.3 -> 0.7 // Moderate signal
            else -> 0.5 // Weak signal
        }
        
        val volatilityConfidence = when {
            volatility > 0.5 || volatility < 0.2 -> 0.8
            else -> 0.6
        }
        
        // Weighted average
        return (densityConfidence * 0.6 + volatilityConfidence * 0.4).coerceIn(0.5, 0.95)
    }

    /**
     * Estimate timeframe from file metadata or filename patterns
     * High confidence when explicit timeframe labels found
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
            else -> Timeframe.H1 // Default to 1 hour
        }
        
        return TimeframeEstimate(
            timeframe = timeframe,
            confidence = if (timeframe == Timeframe.H1) 0.5 else 0.95,
            barDensity = 0.0,
            volatilityScore = 0.0
        )
    }

    /**
     * Combined estimation using both image analysis and filename
     * Returns the estimate with highest confidence
     */
    fun estimateCombined(bitmap: Bitmap, filename: String): TimeframeEstimate {
        val imageEstimate = estimateFromBitmap(bitmap)
        val filenameEstimate = estimateFromFilename(filename)
        
        // If filename has high confidence, use it
        if (filenameEstimate.confidence > 0.9) {
            return filenameEstimate
        }
        
        // If image analysis has good confidence, use it
        if (imageEstimate.confidence > 0.7) {
            return imageEstimate
        }
        
        // If both are weak, prefer filename but note low confidence
        return filenameEstimate.copy(confidence = 0.6)
    }

    data class TimeframeEstimate(
        val timeframe: Timeframe,
        val confidence: Double,
        val barDensity: Double = 0.0,
        val volatilityScore: Double = 0.0
    )
}
