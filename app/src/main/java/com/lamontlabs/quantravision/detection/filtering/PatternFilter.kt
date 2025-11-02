package com.lamontlabs.quantravision.detection.filtering

import com.lamontlabs.quantravision.PatternMatch

enum class PatternType {
    REVERSAL,
    CONTINUATION,
    BREAKOUT,
    ALL
}

enum class ConfidenceLevel {
    LOW,
    MEDIUM,
    HIGH,
    ALL
}

enum class InvalidationStatus {
    ACTIVE,
    INVALIDATED,
    ALL
}

data class PatternFilter(
    val patternTypes: Set<PatternType> = setOf(PatternType.ALL),
    val confidenceLevels: Set<ConfidenceLevel> = setOf(ConfidenceLevel.ALL),
    val timeframes: Set<String> = emptySet(),
    val invalidationStatus: Set<InvalidationStatus> = setOf(InvalidationStatus.ALL)
) {
    
    fun matches(pattern: PatternMatch): Boolean {
        return matchesPatternType(pattern) &&
               matchesConfidence(pattern) &&
               matchesTimeframe(pattern) &&
               matchesInvalidationStatus(pattern)
    }
    
    private fun matchesPatternType(pattern: PatternMatch): Boolean {
        if (patternTypes.contains(PatternType.ALL)) return true
        
        val patternType = classifyPatternType(pattern.patternName)
        return patternTypes.contains(patternType)
    }
    
    private fun matchesConfidence(pattern: PatternMatch): Boolean {
        if (confidenceLevels.contains(ConfidenceLevel.ALL)) return true
        
        val confidenceLevel = when {
            pattern.confidence >= 0.8 -> ConfidenceLevel.HIGH
            pattern.confidence >= 0.6 -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
        
        return confidenceLevels.contains(confidenceLevel)
    }
    
    private fun matchesTimeframe(pattern: PatternMatch): Boolean {
        if (timeframes.isEmpty()) return true
        return timeframes.contains(pattern.timeframe)
    }
    
    private fun matchesInvalidationStatus(pattern: PatternMatch): Boolean {
        if (invalidationStatus.contains(InvalidationStatus.ALL)) return true
        return true
    }
    
    fun isEmpty(): Boolean {
        return patternTypes.contains(PatternType.ALL) &&
               confidenceLevels.contains(ConfidenceLevel.ALL) &&
               timeframes.isEmpty() &&
               invalidationStatus.contains(InvalidationStatus.ALL)
    }
    
    companion object {
        fun default(): PatternFilter {
            return PatternFilter(
                patternTypes = setOf(PatternType.ALL),
                confidenceLevels = setOf(ConfidenceLevel.ALL),
                timeframes = emptySet(),
                invalidationStatus = setOf(InvalidationStatus.ALL)
            )
        }
        
        private fun classifyPatternType(patternName: String): PatternType {
            val name = patternName.lowercase()
            
            return when {
                name.contains("head_and_shoulders") ||
                name.contains("inverse_head_and_shoulders") ||
                name.contains("double_top") ||
                name.contains("double_bottom") ||
                name.contains("triple_top") ||
                name.contains("triple_bottom") ||
                name.contains("rounding_top") ||
                name.contains("rounding_bottom") ||
                name.contains("v_top") ||
                name.contains("v_bottom") -> PatternType.REVERSAL
                
                name.contains("flag") ||
                name.contains("pennant") ||
                name.contains("wedge") ||
                name.contains("rectangle") -> PatternType.CONTINUATION
                
                name.contains("triangle") ||
                name.contains("channel") ||
                name.contains("breakout") -> PatternType.BREAKOUT
                
                else -> PatternType.ALL
            }
        }
    }
}
