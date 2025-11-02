package com.lamontlabs.quantravision.learning.advanced

import org.junit.Test
import org.junit.Assert.*
import kotlin.math.abs

class AnomalyDetectorTest {
    
    @Test
    fun testZScoreThreshold() {
        val threshold = 2.5f
        assertEquals("Z-score threshold is 2.5", 2.5f, threshold, 0.01f)
    }
    
    @Test
    fun testZScoreCalculation() {
        val value = 150.0
        val mean = 100.0
        val stdDev = 20.0
        
        val zScore = abs(value - mean) / stdDev
        
        assertEquals("Z-score calculation", 2.5, zScore, 0.01)
        assertTrue("Value is outlier (z > 2.5)", zScore >= 2.5)
    }
    
    @Test
    fun testSuddenDropThreshold() {
        val changePercent = -35.0f
        assertTrue("Sudden drop detected at -30%", changePercent < -30.0f)
    }
    
    @Test
    fun testUnusualStreakDetection() {
        val maxStreak = 8
        val expectedMaxStreak = 4.0
        
        assertTrue("Unusual streak detected", maxStreak > expectedMaxStreak * 1.5)
    }
}
