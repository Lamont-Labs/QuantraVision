package com.lamontlabs.quantravision.analytics

import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.analytics.model.WinRateStats
import org.junit.Test
import org.junit.Assert.*

class AnalyticsCalculationsTest {
    
    @Test
    fun testWinRateCalculation() {
        val stats = WinRateStats(
            patternName = "Head and Shoulders",
            totalOutcomes = 10,
            wins = 7,
            losses = 2,
            neutral = 1,
            winRate = 0.7,
            avgProfitLoss = 3.5
        )
        
        assertEquals(0.7, stats.winRate, 0.001)
        assertEquals(0.2, stats.lossRate, 0.001)
        assertEquals(0.1, stats.neutralRate, 0.001)
    }
    
    @Test
    fun testWinRateWithZeroOutcomes() {
        val stats = WinRateStats(
            patternName = "Double Top",
            totalOutcomes = 0,
            wins = 0,
            losses = 0,
            neutral = 0,
            winRate = 0.0,
            avgProfitLoss = 0.0
        )
        
        assertEquals(0.0, stats.winRate, 0.001)
        assertEquals(0.0, stats.lossRate, 0.001)
        assertEquals(0.0, stats.neutralRate, 0.001)
    }
    
    @Test
    fun testPerfectWinRate() {
        val stats = WinRateStats(
            patternName = "Bull Flag",
            totalOutcomes = 5,
            wins = 5,
            losses = 0,
            neutral = 0,
            winRate = 1.0,
            avgProfitLoss = 5.2
        )
        
        assertEquals(1.0, stats.winRate, 0.001)
        assertEquals(0.0, stats.lossRate, 0.001)
    }
    
    @Test
    fun testAllLossesRate() {
        val stats = WinRateStats(
            patternName = "Failed Pattern",
            totalOutcomes = 3,
            wins = 0,
            losses = 3,
            neutral = 0,
            winRate = 0.0,
            avgProfitLoss = -4.5
        )
        
        assertEquals(0.0, stats.winRate, 0.001)
        assertEquals(1.0, stats.lossRate, 0.001)
    }
    
    @Test
    fun testOutcomeSumsCorrectly() {
        val stats = WinRateStats(
            patternName = "Triangle",
            totalOutcomes = 20,
            wins = 12,
            losses = 5,
            neutral = 3,
            winRate = 0.6,
            avgProfitLoss = 2.1
        )
        
        assertEquals(20, stats.wins + stats.losses + stats.neutral)
        assertEquals(stats.totalOutcomes, stats.wins + stats.losses + stats.neutral)
    }
    
    @Test
    fun testNegativeAvgProfitLoss() {
        val stats = WinRateStats(
            patternName = "Bad Pattern",
            totalOutcomes = 10,
            wins = 3,
            losses = 7,
            neutral = 0,
            winRate = 0.3,
            avgProfitLoss = -2.5
        )
        
        assertTrue(stats.avgProfitLoss < 0)
        assertTrue(stats.lossRate > stats.winRate)
    }
}
