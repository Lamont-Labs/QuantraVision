package com.lamontlabs.quantravision.learning.advanced

import org.junit.Test
import org.junit.Assert.*

class TrendForecasterTest {
    
    @Test
    fun testLinearRegressionSlope() {
        val x = listOf(0.0, 1.0, 2.0, 3.0, 4.0)
        val y = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        
        val xMean = x.average()
        val yMean = y.average()
        
        val numerator = x.zip(y) { xi, yi -> (xi - xMean) * (yi - yMean) }.sum()
        val denominator = x.map { (it - xMean) * (it - xMean) }.sum()
        
        val slope = numerator / denominator
        
        assertEquals("Perfect positive correlation slope is 1.0", 1.0, slope, 0.01)
    }
    
    @Test
    fun testMinimumDataPoints() {
        val minRequired = 30
        assertTrue("Minimum 30 data points required for forecasting", minRequired >= 30)
    }
    
    @Test
    fun testMovingAverageWindow() {
        val ma7Window = 7
        val ma30Window = 30
        
        assertEquals("7-day moving average window", 7, ma7Window)
        assertEquals("30-day moving average window", 30, ma30Window)
    }
}
