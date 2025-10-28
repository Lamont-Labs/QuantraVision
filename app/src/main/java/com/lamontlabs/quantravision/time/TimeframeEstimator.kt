package com.lamontlabs.quantravision.time

import android.graphics.Bitmap

/**
 * TimeframeEstimator
 * Estimates the timeframe of a chart from its bitmap
 * Used during pattern detection to classify patterns by timeframe
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
     * Uses heuristics like image dimensions, bar count, etc.
     */
    fun estimateFromBitmap(bitmap: Bitmap): TimeframeEstimate {
        val width = bitmap.width
        val height = bitmap.height
        
        // Simple heuristic: larger images tend to be longer timeframes
        // In production, this would analyze actual chart features
        val timeframe = when {
            width < 800 -> Timeframe.M1
            width < 1200 -> Timeframe.M5
            width < 1600 -> Timeframe.M15
            width < 2000 -> Timeframe.H1
            width < 2400 -> Timeframe.H4
            else -> Timeframe.D1
        }
        
        return TimeframeEstimate(
            timeframe = timeframe,
            confidence = 0.85
        )
    }

    /**
     * Estimate timeframe from file metadata or filename patterns
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
            confidence = if (timeframe == Timeframe.H1) 0.5 else 0.95
        )
    }

    data class TimeframeEstimate(
        val timeframe: Timeframe,
        val confidence: Double
    )
}
