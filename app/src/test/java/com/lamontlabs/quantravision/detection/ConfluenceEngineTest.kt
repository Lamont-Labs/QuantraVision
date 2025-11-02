package com.lamontlabs.quantravision.detection

import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.detection.confluence.ConfluenceEngine
import com.lamontlabs.quantravision.detection.confluence.ConfluenceZone
import org.junit.Test
import org.junit.Assert.*

class ConfluenceEngineTest {
    
    private fun createMockPattern(
        id: Int,
        name: String,
        confidence: Double,
        bounds: String
    ): PatternMatch {
        return PatternMatch(
            id = id,
            patternName = name,
            confidence = confidence,
            timestamp = System.currentTimeMillis(),
            timeframe = "M5",
            scale = 1.0,
            consensusScore = confidence,
            windowMs = 7000L,
            originPath = "test",
            detectionBounds = bounds
        )
    }
    
    @Test
    fun testEmptyPatternsReturnsEmptyZones() {
        val zones = ConfluenceEngine.findConfluenceZones(emptyList())
        assertTrue(zones.isEmpty())
    }
    
    @Test
    fun testSinglePatternReturnsNoZones() {
        val pattern = createMockPattern(1, "Head and Shoulders", 0.85, "100,100,50,50")
        val zones = ConfluenceEngine.findConfluenceZones(listOf(pattern))
        assertTrue(zones.isEmpty())
    }
    
    @Test
    fun testTwoNearbyPatternsCreateConfluence() {
        val pattern1 = createMockPattern(1, "Double Top", 0.8, "100,100,50,50")
        val pattern2 = createMockPattern(2, "Head and Shoulders", 0.75, "110,110,50,50")
        
        val zones = ConfluenceEngine.findConfluenceZones(listOf(pattern1, pattern2), gridSize = 50)
        
        assertFalse(zones.isEmpty())
        assertTrue(zones.any { it.patternCount >= 2 })
    }
    
    @Test
    fun testConfluenceStrengthCalculation() {
        val pattern1 = createMockPattern(1, "Pattern A", 0.8, "100,100,50,50")
        val pattern2 = createMockPattern(2, "Pattern B", 0.75, "100,100,50,50")
        
        val strength = ConfluenceEngine.calculateStrength(listOf(pattern1, pattern2))
        
        assertTrue(strength >= 1.5f)
    }
    
    @Test
    fun testThreePatternsIncreaseStrength() {
        val patterns = listOf(
            createMockPattern(1, "Pattern A", 0.8, "100,100,50,50"),
            createMockPattern(2, "Pattern B", 0.75, "100,100,50,50"),
            createMockPattern(3, "Pattern C", 0.7, "100,100,50,50")
        )
        
        val strength = ConfluenceEngine.calculateStrength(patterns)
        
        assertTrue(strength >= 2.0f)
    }
    
    @Test
    fun testHighConfidencePatternsIncreaseStrength() {
        val highConfPatterns = listOf(
            createMockPattern(1, "Pattern A", 0.95, "100,100,50,50"),
            createMockPattern(2, "Pattern B", 0.90, "100,100,50,50")
        )
        
        val lowConfPatterns = listOf(
            createMockPattern(3, "Pattern C", 0.65, "100,100,50,50"),
            createMockPattern(4, "Pattern D", 0.60, "100,100,50,50")
        )
        
        val highStrength = ConfluenceEngine.calculateStrength(highConfPatterns)
        val lowStrength = ConfluenceEngine.calculateStrength(lowConfPatterns)
        
        assertTrue(highStrength > lowStrength)
    }
    
    @Test
    fun testConfluenceZoneDescription() {
        val zone = ConfluenceZone(
            patterns = listOf(
                createMockPattern(1, "Pattern A", 0.8, "100,100,50,50"),
                createMockPattern(2, "Pattern B", 0.75, "100,100,50,50")
            ),
            centerX = 125.0,
            centerY = 125.0,
            strength = 1.5f
        )
        
        assertEquals("2× Confluence", zone.description)
        assertEquals(2, zone.patternCount)
    }
    
    @Test
    fun testConfluenceZoneStrengthMultiplier() {
        val twoPatternZone = ConfluenceZone(
            patterns = listOf(
                createMockPattern(1, "A", 0.8, "100,100,50,50"),
                createMockPattern(2, "B", 0.75, "100,100,50,50")
            ),
            centerX = 100.0,
            centerY = 100.0,
            strength = 1.5f
        )
        
        val threePatternZone = ConfluenceZone(
            patterns = listOf(
                createMockPattern(1, "A", 0.8, "100,100,50,50"),
                createMockPattern(2, "B", 0.75, "100,100,50,50"),
                createMockPattern(3, "C", 0.7, "100,100,50,50")
            ),
            centerX = 100.0,
            centerY = 100.0,
            strength = 2.0f
        )
        
        assertEquals(1.5f, twoPatternZone.strengthMultiplier, 0.01f)
        assertEquals(2.0f, threePatternZone.strengthMultiplier, 0.01f)
    }
    
    @Test
    fun testIsHighStrengthZone() {
        val highStrengthZone = ConfluenceZone(
            patterns = listOf(
                createMockPattern(1, "A", 0.9, "100,100,50,50"),
                createMockPattern(2, "B", 0.85, "100,100,50,50")
            ),
            centerX = 100.0,
            centerY = 100.0,
            strength = 1.9f
        )
        
        val lowStrengthZone = ConfluenceZone(
            patterns = listOf(
                createMockPattern(1, "A", 0.65, "100,100,50,50"),
                createMockPattern(2, "B", 0.60, "100,100,50,50")
            ),
            centerX = 100.0,
            centerY = 100.0,
            strength = 1.2f
        )
        
        assertTrue(ConfluenceEngine.isHighStrengthZone(highStrengthZone))
        assertFalse(ConfluenceEngine.isHighStrengthZone(lowStrengthZone))
    }
}
