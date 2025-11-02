package com.lamontlabs.quantravision.filtering

import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.detection.filtering.*
import org.junit.Test
import org.junit.Assert.*

class PatternFilterTest {
    
    @Test
    fun testDefaultFilter() {
        val filter = PatternFilter.default()
        
        assertTrue(filter.isEmpty())
        assertTrue(filter.patternTypes.contains(PatternType.ALL))
        assertTrue(filter.confidenceLevels.contains(ConfidenceLevel.ALL))
        assertTrue(filter.timeframes.isEmpty())
    }
    
    @Test
    fun testMatchesHighConfidencePattern() {
        val filter = PatternFilter(
            patternTypes = setOf(PatternType.ALL),
            confidenceLevels = setOf(ConfidenceLevel.HIGH),
            timeframes = emptySet(),
            invalidationStatus = setOf(InvalidationStatus.ALL)
        )
        
        val pattern = PatternMatch(
            patternName = "head_and_shoulders",
            confidence = 0.92,
            timestamp = System.currentTimeMillis(),
            timeframe = "1h",
            scale = 1.0,
            consensusScore = 0.9,
            windowMs = 5000L
        )
        
        assertTrue(filter.matches(pattern))
    }
    
    @Test
    fun testMatchesLowConfidencePattern() {
        val filter = PatternFilter(
            patternTypes = setOf(PatternType.ALL),
            confidenceLevels = setOf(ConfidenceLevel.LOW),
            timeframes = emptySet(),
            invalidationStatus = setOf(InvalidationStatus.ALL)
        )
        
        val pattern = PatternMatch(
            patternName = "double_top",
            confidence = 0.45,
            timestamp = System.currentTimeMillis(),
            timeframe = "1h",
            scale = 1.0,
            consensusScore = 0.5,
            windowMs = 5000L
        )
        
        assertTrue(filter.matches(pattern))
    }
    
    @Test
    fun testMatchesTimeframe() {
        val filter = PatternFilter(
            patternTypes = setOf(PatternType.ALL),
            confidenceLevels = setOf(ConfidenceLevel.ALL),
            timeframes = setOf("1h", "4h"),
            invalidationStatus = setOf(InvalidationStatus.ALL)
        )
        
        val pattern1h = PatternMatch(
            patternName = "triangle",
            confidence = 0.75,
            timestamp = System.currentTimeMillis(),
            timeframe = "1h",
            scale = 1.0,
            consensusScore = 0.7,
            windowMs = 5000L
        )
        
        val pattern15m = PatternMatch(
            patternName = "triangle",
            confidence = 0.75,
            timestamp = System.currentTimeMillis(),
            timeframe = "15m",
            scale = 1.0,
            consensusScore = 0.7,
            windowMs = 5000L
        )
        
        assertTrue(filter.matches(pattern1h))
        assertFalse(filter.matches(pattern15m))
    }
    
    @Test
    fun testMatchesPatternType() {
        val filter = PatternFilter(
            patternTypes = setOf(PatternType.REVERSAL),
            confidenceLevels = setOf(ConfidenceLevel.ALL),
            timeframes = emptySet(),
            invalidationStatus = setOf(InvalidationStatus.ALL)
        )
        
        val reversalPattern = PatternMatch(
            patternName = "head_and_shoulders",
            confidence = 0.85,
            timestamp = System.currentTimeMillis(),
            timeframe = "1h",
            scale = 1.0,
            consensusScore = 0.8,
            windowMs = 5000L
        )
        
        val continuationPattern = PatternMatch(
            patternName = "bull_flag",
            confidence = 0.85,
            timestamp = System.currentTimeMillis(),
            timeframe = "1h",
            scale = 1.0,
            consensusScore = 0.8,
            windowMs = 5000L
        )
        
        assertTrue(filter.matches(reversalPattern))
        assertFalse(filter.matches(continuationPattern))
    }
    
    @Test
    fun testFilterIsEmpty() {
        val defaultFilter = PatternFilter.default()
        assertTrue(defaultFilter.isEmpty())
        
        val customFilter = PatternFilter(
            patternTypes = setOf(PatternType.REVERSAL),
            confidenceLevels = setOf(ConfidenceLevel.HIGH),
            timeframes = setOf("1h"),
            invalidationStatus = setOf(InvalidationStatus.ACTIVE)
        )
        assertFalse(customFilter.isEmpty())
    }
}
