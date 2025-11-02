package com.lamontlabs.quantravision.detection.confluence

import com.lamontlabs.quantravision.PatternMatch

data class ConfluenceZone(
    val patterns: List<PatternMatch>,
    val centerX: Double,
    val centerY: Double,
    val strength: Float
) {
    val patternCount: Int get() = patterns.size
    val uniquePatternTypes: Int get() = patterns.map { it.patternName }.toSet().size
    
    val strengthMultiplier: Float get() = when (patternCount) {
        0, 1 -> 1.0f
        2 -> 1.5f
        else -> 2.0f
    }
    
    val description: String get() = when {
        patternCount == 0 -> "No confluence"
        patternCount == 1 -> "Single pattern"
        patternCount == 2 -> "2× Confluence"
        patternCount == 3 -> "3× Confluence"
        else -> "${patternCount}× Confluence"
    }
    
    val avgConfidence: Double get() = if (patterns.isNotEmpty()) {
        patterns.map { it.confidence }.average()
    } else 0.0
}
