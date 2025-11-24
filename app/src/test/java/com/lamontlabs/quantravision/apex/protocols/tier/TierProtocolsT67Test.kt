package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T67MultiFrameContinuationFusion
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TierProtocolsT67Test {
    
    private lateinit var protocol: T67MultiFrameContinuationFusion
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T67MultiFrameContinuationFusion()
        state = mutableMapOf()
    }
    
    private fun createTestCandles(count: Int, basePrice: Double = 100.0): List<Candle> {
        return (0 until count).map { i ->
            Candle(
                timestamp = 1000000L + (i * 60000L),
                open = basePrice + (i * 0.1),
                high = basePrice + (i * 0.1) + 0.5,
                low = basePrice + (i * 0.1) - 0.5,
                close = basePrice + (i * 0.1) + 0.2,
                volume = 10000.0 + (i * 100.0)
            )
        }
    }
    
    @Test
    fun testProtocolId() {
        assertEquals("T67", protocol.protocolId)
        assertEquals("MultiFrameContinuationFusion", protocol.protocolName)
        assertEquals(3.15, protocol.weight, 0.01)
    }
    
    @Test
    fun testInsufficientCandles() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(30)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("Insufficient candles"))
        assertEquals(0.0, state["multiFrameContinuationScore"])
        assertEquals(false, state["multiFrameFused"])
    }
    
    @Test
    fun testHighScoreNoDrift() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(40)
        
        state["finalContinuationScore"] = 0.9
        state["consistencyScore"] = 0.85
        state["continuationStrengthScore"] = 0.8
        state["driftScore"] = 0.1
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(state["multiFrameFused"] as Boolean)
        val score = state["multiFrameContinuationScore"] as Double
        assertTrue(score >= 0.7)
    }
    
    @Test
    fun testHighScoreWithDrift() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(40)
        
        state["finalContinuationScore"] = 0.9
        state["consistencyScore"] = 0.85
        state["continuationStrengthScore"] = 0.8
        state["driftScore"] = 0.5
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        val score = state["multiFrameContinuationScore"] as Double
        val base = 0.4 * 0.9 + 0.3 * 0.85 + 0.3 * 0.8
        val expected = base * (1.0 - 0.2 * 0.5)
        
        assertEquals(expected, score, 0.01)
    }
    
    @Test
    fun testLowScores() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(40)
        
        state["finalContinuationScore"] = 0.3
        state["consistencyScore"] = 0.2
        state["continuationStrengthScore"] = 0.25
        state["driftScore"] = 0.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertFalse(state["multiFrameFused"] as Boolean)
        val score = state["multiFrameContinuationScore"] as Double
        assertTrue(score < 0.7)
    }
    
    @Test
    fun testFailClosedDefaults() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(40)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, state["multiFrameContinuationScore"])
        assertEquals(false, state["multiFrameFused"])
    }
    
    @Test
    fun testDriftPenalty() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(40)
        
        state["finalContinuationScore"] = 0.8
        state["consistencyScore"] = 0.8
        state["continuationStrengthScore"] = 0.8
        state["driftScore"] = 0.0
        
        val verdict1 = protocol.evaluate(context, primitives, state.toMutableMap())
        val scoreNoDrift = (state["multiFrameContinuationScore"] as Double)
        
        state["driftScore"] = 0.5
        val verdict2 = protocol.evaluate(context, primitives, state.toMutableMap())
        val scoreWithDrift = (state["multiFrameContinuationScore"] as Double)
        
        assertTrue(scoreNoDrift > scoreWithDrift)
    }
    
    @Test
    fun testDeterminism() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(40)
        
        val testState = mutableMapOf<String, Any>(
            "finalContinuationScore" to 0.75,
            "consistencyScore" to 0.7,
            "continuationStrengthScore" to 0.72,
            "driftScore" to 0.3
        )
        
        val verdict1 = protocol.evaluate(context, primitives, testState.toMutableMap())
        val verdict2 = protocol.evaluate(context, primitives, testState.toMutableMap())
        val verdict3 = protocol.evaluate(context, primitives, testState.toMutableMap())
        
        assertEquals(verdict1.passed, verdict2.passed)
        assertEquals(verdict1.passed, verdict3.passed)
        assertEquals(verdict1.confidence, verdict2.confidence, 0.0001)
        assertEquals(verdict1.confidence, verdict3.confidence, 0.0001)
    }
    
    private fun createTestContext(): ApexScanContext {
        return ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user",
            tier = SubscriptionTier.PRO
        )
    }
    
    private fun createTestPrimitives(candleCount: Int): ChartPrimitives {
        return ChartPrimitives(
            rawImageHash = "test_hash",
            candles = createTestCandles(candleCount),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
    }
}
