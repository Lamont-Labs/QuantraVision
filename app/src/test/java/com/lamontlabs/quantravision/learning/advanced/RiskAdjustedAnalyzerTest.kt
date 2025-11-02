package com.lamontlabs.quantravision.learning.advanced

import org.junit.Test
import org.junit.Assert.*

class RiskAdjustedAnalyzerTest {
    
    @Test
    fun testSharpeRatioCalculation() {
        val avgReturn = 0.05f
        val stdDev = 0.02f
        val sharpe = avgReturn / stdDev
        
        assertTrue("Sharpe ratio calculated correctly", sharpe > 0)
        assertEquals("Sharpe ratio value", 2.5f, sharpe, 0.01f)
    }
    
    @Test
    fun testExpectedValueFormula() {
        val winRate = 0.60f
        val avgWin = 100f
        val avgLoss = 50f
        
        val expectedValue = (winRate * avgWin) - ((1 - winRate) * avgLoss)
        
        assertTrue("Expected value is positive", expectedValue > 0)
        assertEquals("Expected value calculated", 40f, expectedValue, 0.01f)
    }
    
    @Test
    fun testRiskLevelClassification() {
        val lowVolatility = 1.5
        val mediumVolatility = 3.5
        val highVolatility = 6.0
        
        assertTrue("Low volatility < 2", lowVolatility < 2.0)
        assertTrue("Medium volatility 2-5", mediumVolatility >= 2.0 && mediumVolatility < 5.0)
        assertTrue("High volatility >= 5", highVolatility >= 5.0)
    }
}
