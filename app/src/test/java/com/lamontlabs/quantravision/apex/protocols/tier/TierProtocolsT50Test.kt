package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T50CrossRegimeCoherence
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TierProtocolsT50Test {
    
    private lateinit var protocol: T50CrossRegimeCoherence
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T50CrossRegimeCoherence()
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
        assertEquals("T50", protocol.protocolId)
        assertEquals("CrossRegimeCoherence", protocol.protocolName)
        assertEquals(2.8, protocol.weight, 0.01)
    }
    
    @Test
    fun testInsufficientCandles() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(20)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("Insufficient candles"))
        assertEquals(false, state["crossRegimeCoherent"])
        assertEquals(0.0, state["regimeCoherenceScore"])
    }
    
    @Test
    fun testAllRegimeChecksPass() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["regimeAlignmentOk"] = true
        state["sectorCompatible"] = true
        state["volatilityRegimeMatch"] = true
        state["regimeStable"] = true
        state["mtfCoherenceScore"] = 0.85
        state["regimeAlignmentScore"] = 0.80
        state["sectorCompatibilityScore"] = 0.75
        state["volatilityRegimeScore"] = 0.78
        state["regimeStabilityScore"] = 0.82
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(state["crossRegimeCoherent"] as Boolean)
        assertTrue((state["regimeCoherenceScore"] as Double) > 0.7)
    }
    
    @Test
    fun testPartialRegimeCoherence() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["regimeAlignmentOk"] = true
        state["sectorCompatible"] = true
        state["volatilityRegimeMatch"] = false
        state["regimeStable"] = false
        state["mtfCoherenceScore"] = 0.65
        state["regimeAlignmentScore"] = 0.70
        state["sectorCompatibilityScore"] = 0.68
        state["volatilityRegimeScore"] = 0.45
        state["regimeStabilityScore"] = 0.42
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertFalse(state["crossRegimeCoherent"] as Boolean)
    }
    
    @Test
    fun testMostRegimeChecksFail() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["regimeAlignmentOk"] = false
        state["sectorCompatible"] = false
        state["volatilityRegimeMatch"] = false
        state["regimeStable"] = true
        state["mtfCoherenceScore"] = 0.50
        state["regimeAlignmentScore"] = 0.40
        state["sectorCompatibilityScore"] = 0.38
        state["volatilityRegimeScore"] = 0.35
        state["regimeStabilityScore"] = 0.72
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertFalse(state["crossRegimeCoherent"] as Boolean)
    }
    
    @Test
    fun testDeterminism() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        val testState = mutableMapOf<String, Any>(
            "regimeAlignmentOk" to true,
            "sectorCompatible" to true,
            "volatilityRegimeMatch" to true,
            "regimeStable" to false,
            "mtfCoherenceScore" to 0.75,
            "regimeAlignmentScore" to 0.70,
            "sectorCompatibilityScore" to 0.72,
            "volatilityRegimeScore" to 0.68,
            "regimeStabilityScore" to 0.55
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
