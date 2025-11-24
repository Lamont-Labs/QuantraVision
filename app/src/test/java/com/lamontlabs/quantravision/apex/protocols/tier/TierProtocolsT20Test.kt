package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for T20: FinalEntropyCheck protocol.
 * 
 * Tests:
 * - Entropy aggregation from previous protocols
 * - Final entropy classification
 * - State mutations
 * - Integration with other protocol state
 */
class TierProtocolsT20Test {
    
    private lateinit var protocol: T20FinalEntropyCheck
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T20FinalEntropyCheck()
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
        assertEquals("T20", protocol.protocolId)
        assertEquals("FinalEntropyCheck", protocol.protocolName)
    }
    
    @Test
    fun testLowEntropy() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "low_entropy",
            candles = createTestCandles(count = 30),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        // Simulate state from previous protocols indicating low entropy
        state["entropyScore"] = 0.20
        state["conflictCount"] = 0
        state["signalClarity"] = 0.85
        state["regimeMatch"] = true
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(verdict.confidence > 0.8)
        assertTrue(verdict.reason.contains("PASS"))
        
        // Verify state mutations
        val finalEntropy = state["finalEntropy"] as Double
        assertTrue(finalEntropy < 0.60)
        
        val finalEntropyOk = state["finalEntropyOk"] as Boolean
        assertTrue(finalEntropyOk)
    }
    
    @Test
    fun testHighEntropy() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "high_entropy",
            candles = createTestCandles(count = 30),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        // Simulate state from previous protocols indicating high entropy
        state["entropyScore"] = 0.70
        state["conflictCount"] = 3
        state["signalClarity"] = 0.30
        state["regimeMatch"] = false
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertTrue(verdict.confidence < 0.5)
        assertTrue(verdict.reason.contains("FAIL") || verdict.reason.contains("WARN"))
        
        // Verify state mutations
        val finalEntropy = state["finalEntropy"] as Double
        assertTrue(finalEntropy > 0.60)
        
        val finalEntropyOk = state["finalEntropyOk"] as Boolean
        assertFalse(finalEntropyOk)
    }
    
    @Test
    fun testMissingPreviousState() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "missing_state",
            candles = createTestCandles(count = 30),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        // Empty state (simulating missing previous protocol data)
        val verdict = protocol.evaluate(context, primitives, state)
        
        // Should handle gracefully with defaults
        assertNotNull(verdict)
        assertTrue(state.containsKey("finalEntropy"))
        assertTrue(state.containsKey("finalEntropyOk"))
    }
    
    @Test
    fun testDeterminism() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "determinism_test",
            candles = createTestCandles(count = 25),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        // Set up identical state for multiple runs
        val state1 = mutableMapOf<String, Any>(
            "entropyScore" to 0.40,
            "conflictCount" to 1,
            "signalClarity" to 0.65,
            "regimeMatch" to true
        )
        
        val state2 = mutableMapOf<String, Any>(
            "entropyScore" to 0.40,
            "conflictCount" to 1,
            "signalClarity" to 0.65,
            "regimeMatch" to true
        )
        
        val state3 = mutableMapOf<String, Any>(
            "entropyScore" to 0.40,
            "conflictCount" to 1,
            "signalClarity" to 0.65,
            "regimeMatch" to true
        )
        
        val verdict1 = protocol.evaluate(context, primitives, state1)
        val verdict2 = protocol.evaluate(context, primitives, state2)
        val verdict3 = protocol.evaluate(context, primitives, state3)
        
        // All results should be identical
        assertEquals(verdict1.passed, verdict2.passed)
        assertEquals(verdict1.passed, verdict3.passed)
        assertEquals(verdict1.confidence, verdict2.confidence, 0.0001)
        assertEquals(verdict1.confidence, verdict3.confidence, 0.0001)
        assertEquals(verdict1.reason, verdict2.reason)
        assertEquals(verdict1.reason, verdict3.reason)
        
        // Final entropy should be identical
        val finalEntropy1 = state1["finalEntropy"] as Double
        val finalEntropy2 = state2["finalEntropy"] as Double
        val finalEntropy3 = state3["finalEntropy"] as Double
        
        assertEquals(finalEntropy1, finalEntropy2, 0.0001)
        assertEquals(finalEntropy1, finalEntropy3, 0.0001)
    }
    
    @Test
    fun testEntropyAggregation() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "aggregation_test",
            candles = createTestCandles(count = 30),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        // Test different combinations of state
        state["entropyScore"] = 0.50
        state["conflictCount"] = 2
        state["signalClarity"] = 0.50
        state["regimeMatch"] = true
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        // Verify aggregation logic
        val finalEntropy = state["finalEntropy"] as Double
        assertTrue(finalEntropy >= 0.0 && finalEntropy <= 1.0)
        
        // Final entropy should be influenced by all factors
        val earlyEntropy = state["entropyScore"] as Double
        assertTrue(finalEntropy >= earlyEntropy * 0.35) // Should incorporate early entropy
    }
    
    @Test
    fun testReasonMessageComponents() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "reason_test",
            candles = createTestCandles(count = 30),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        state["entropyScore"] = 0.45
        state["conflictCount"] = 1
        state["signalClarity"] = 0.70
        state["regimeMatch"] = true
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        // Verify reason message contains expected components
        assertTrue(verdict.reason.contains("Final entropy:"))
        assertTrue(verdict.reason.contains("early=") || verdict.reason.contains("conflicts=") ||
                   verdict.reason.contains("clarity=") || verdict.reason.contains("regime="))
    }
}
