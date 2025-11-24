package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T55AdaptiveSuppressionThreshold
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TierProtocolsT55Test {
    
    private lateinit var protocol: T55AdaptiveSuppressionThreshold
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T55AdaptiveSuppressionThreshold()
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
        assertEquals("T55", protocol.protocolId)
        assertEquals("AdaptiveSuppressionThreshold", protocol.protocolName)
        assertEquals(2.9, protocol.weight, 0.01)
    }
    
    @Test
    fun testInsufficientCandles() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(20)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("Insufficient candles"))
        assertEquals(true, state["adaptiveSuppressionActive"])
        assertEquals(1.0, state["finalSuppressionScore"])
    }
    
    @Test
    fun testNoSuppression() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(30)
        
        state["suppressionActive"] = false
        state["patternSuppressed"] = false
        state["noiseSuppressed"] = false
        state["conflictSuppressed"] = false
        state["suppressionScore"] = 0.2
        state["patternSuppressionScore"] = 0.2
        state["noiseSuppressionLevel"] = 0.2
        state["conflictSuppressionScore"] = 0.15
        state["adaptiveThreshold"] = 0.65
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertFalse(state["adaptiveSuppressionActive"] as Boolean)
        assertTrue((state["finalSuppressionScore"] as Double) < 0.5)
    }
    
    @Test
    fun testMultipleSuppressionTriggers() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(30)
        
        state["suppressionActive"] = true
        state["patternSuppressed"] = true
        state["noiseSuppressed"] = false
        state["conflictSuppressed"] = false
        state["suppressionScore"] = 0.7
        state["patternSuppressionScore"] = 0.75
        state["noiseSuppressionLevel"] = 0.4
        state["conflictSuppressionScore"] = 0.3
        state["adaptiveThreshold"] = 0.65
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertTrue(state["adaptiveSuppressionActive"] as Boolean)
    }
    
    @Test
    fun testHighSuppressionScore() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(30)
        
        state["suppressionActive"] = false
        state["patternSuppressed"] = true
        state["noiseSuppressed"] = false
        state["conflictSuppressed"] = false
        state["suppressionScore"] = 0.8
        state["patternSuppressionScore"] = 0.85
        state["noiseSuppressionLevel"] = 0.75
        state["conflictSuppressionScore"] = 0.7
        state["adaptiveThreshold"] = 0.65
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertTrue(state["adaptiveSuppressionActive"] as Boolean)
        assertTrue((state["finalSuppressionScore"] as Double) > 0.65)
    }
    
    @Test
    fun testDeterminism() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(30)
        
        val testState = mutableMapOf<String, Any>(
            "suppressionActive" to true,
            "patternSuppressed" to false,
            "noiseSuppressed" to true,
            "conflictSuppressed" to false,
            "suppressionScore" to 0.6,
            "patternSuppressionScore" to 0.4,
            "noiseSuppressionLevel" to 0.7,
            "conflictSuppressionScore" to 0.3,
            "adaptiveThreshold" to 0.65
        )
        
        val verdict1 = protocol.evaluate(context, primitives, testState.toMutableMap())
        val verdict2 = protocol.evaluate(context, primitives, testState.toMutableMap())
        val verdict3 = protocol.evaluate(context, primitives, testState.toMutableMap())
        
        assertEquals(verdict1.passed, verdict2.passed)
        assertEquals(verdict1.passed, verdict3.passed)
        assertEquals(verdict1.confidence, verdict2.confidence, 0.0001)
        assertEquals(verdict1.confidence, verdict3.confidence, 0.0001)
        assertEquals(verdict1.reason, verdict2.reason)
        assertEquals(verdict1.reason, verdict3.reason)
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
