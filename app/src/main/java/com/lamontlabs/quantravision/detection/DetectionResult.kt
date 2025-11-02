package com.lamontlabs.quantravision.detection

import android.graphics.RectF
import com.lamontlabs.quantravision.PatternMatch

data class DetectionResult(
    val patternName: String,
    val confidence: Double,
    val bbox: RectF? = null,
    val source: String = "Template",
    val reasoning: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val timeframe: String = "unknown"
) {
    fun toPatternMatch(): PatternMatch {
        return PatternMatch(
            patternName = patternName,
            confidence = confidence,
            timestamp = timestamp,
            timeframe = timeframe,
            scale = 1.0,
            consensusScore = confidence,
            windowMs = 0L,
            originPath = "",
            detectionBounds = bbox?.let { "${it.left},${it.top},${it.width()},${it.height()}" }
        )
    }
}
