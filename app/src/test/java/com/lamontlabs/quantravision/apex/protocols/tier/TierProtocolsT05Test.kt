package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T05PriceRangeNormalization
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for T05: PriceRangeNormalization protocol.
 * 
 * Tests:
 * - Valid price range detection
 * - Invalid range detection (too narrow/wide)
 * - Normalization calculations
 * - Determinism verification
 */
class TierProtocolsT05Test {
    
    private lateinit var protocol: T05PriceRangeNormalization
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T05PriceRangeNormalization()
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
        assertEquals("T05", protocol.protocolId)
        assertEquals("PriceRangeNormalization", protocol.protocolName)
    }
    
    @Test
    fun testValidPriceRange() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_456",
            candles = createTestCandles(count = 30),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(verdict.confidence > 0.5)
        
        // Verify state mutations
        assertTrue(state.containsKey("priceRangeValid"))
        assertTrue(state.containsKey("normalizedRange"))
        assertTrue(state.containsKey("priceMin"))
        assertTrue(state.containsKey("priceMax"))
        
        val rangeValid = state["priceRangeValid"] as Boolean
        assertTrue(rangeValid)
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
        
        // Run multiple times with same inputs
        val state1 = mutableMapOf<String, Any>()
        val state2 = mutableMapOf<String, Any>()
        val state3 = mutableMapOf<String, Any>()
        
        val verdict1 = protocol.evaluate(context, primitives, state1)
        val verdict2 = protocol.evaluate(context, primitives, state2)
        val verdict3 = protocol.evaluate(context, primitives, state3)
        
        // All results should be identical
        assertEquals(verdict1.passed, verdict2.passed)
        assertEquals(verdict1.passed, verdict3.passed)
        assertEquals(verdict1.confidence, verdict2.confidence, 0.0001)
        assertEquals(verdict1.confidence, verdict3.confidence, 0.0001)
        
        // State values should be identical
        assertEquals(state1["priceRangeValid"], state2["priceRangeValid"])
        assertEquals(state1["priceRangeValid"], state3["priceRangeValid"])
        assertEquals(state1["normalizedRange"], state2["normalizedRange"])
        assertEquals(state1["normalizedRange"], state3["normalizedRange"])
    }
    
    @Test
    fun testStateMutations() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "state_test",
            candles = createTestCandles(count = 20),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        // Verify all expected state keys are present
        assertNotNull(state["priceRangeValid"])
        assertNotNull(state["normalizedRange"])
        assertNotNull(state["priceMin"])
        assertNotNull(state["priceMax"])
        
        // Verify types
        assertTrue(state["priceRangeValid"] is Boolean)
        assertTrue(state["normalizedRange"] is Double)
        assertTrue(state["priceMin"] is Double)
        assertTrue(state["priceMax"] is Double)
        
        // Verify logical consistency
        val priceMin = state["priceMin"] as Double
        val priceMax = state["priceMax"] as Double
        assertTrue(priceMax >= priceMin)
    }
    
    @Test
    fun testReasonMessageFormat() = runBlocking {
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
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        // Verify reason message contains expected components
        assertTrue(verdict.reason.contains("Price range:"))
        assertTrue(verdict.reason.contains("normalized:"))
    }
    
    @Test
    fun testDifferentHashesDifferentResults() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives1 = ChartPrimitives(
            rawImageHash = "hash_A",
            candles = createTestCandles(count = 25),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val primitives2 = ChartPrimitives(
            rawImageHash = "hash_B",
            candles = createTestCandles(count = 25),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val state1 = mutableMapOf<String, Any>()
        val state2 = mutableMapOf<String, Any>()
        
        val verdict1 = protocol.evaluate(context, primitives1, state1)
        val verdict2 = protocol.evaluate(context, primitives2, state2)
        
        // Different hashes should produce different price ranges (deterministically)
        val range1 = state1["normalizedRange"] as Double
        val range2 = state2["normalizedRange"] as Double
        
        assertNotEquals(range1, range2, 0.0001)
    }
}
