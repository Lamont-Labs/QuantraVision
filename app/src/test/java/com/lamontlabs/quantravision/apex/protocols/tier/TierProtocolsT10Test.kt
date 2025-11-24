package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for T10: StructureCompleteness protocol.
 * 
 * Tests:
 * - Complete structure detection
 * - Incomplete structure detection
 * - Swing point detection
 * - State mutations
 */
class TierProtocolsT10Test {
    
    private lateinit var protocol: T10StructureCompleteness
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T10StructureCompleteness()
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
        assertEquals("T10", protocol.protocolId)
        assertEquals("StructureCompleteness", protocol.protocolName)
    }
    
    @Test
    fun testCompleteStructure() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        // Provide adequate lines and candles for complete structure
        val primitives = ChartPrimitives(
            rawImageHash = "complete_structure",
            candles = createTestCandles(count = 30),
            detectedLines = listOf(
                TrendLine(x1 = 10.0, y1 = 20.0, x2 = 100.0, y2 = 80.0, confidence = 0.9),
                TrendLine(x1 = 20.0, y1 = 30.0, x2 = 110.0, y2 = 90.0, confidence = 0.85)
            ),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        // Should pass if structure is complete
        assertTrue(verdict.confidence > 0.5)
        
        // Verify state mutations
        assertTrue(state.containsKey("structureComplete"))
        assertTrue(state.containsKey("swingPointCount"))
        assertTrue(state.containsKey("requiredElements"))
        assertTrue(state.containsKey("missingElements"))
        
        val structureComplete = state["structureComplete"] as Boolean
        if (structureComplete) {
            assertTrue(verdict.passed)
            assertTrue(verdict.reason.contains("COMPLETE"))
        }
    }
    
    @Test
    fun testIncompleteStructure() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        // Provide insufficient data for complete structure
        val primitives = ChartPrimitives(
            rawImageHash = "incomplete_structure",
            candles = createTestCandles(count = 5), // Too few candles
            detectedLines = emptyList(), // No lines
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertTrue(verdict.reason.contains("INCOMPLETE"))
        
        // Verify state
        val structureComplete = state["structureComplete"] as Boolean
        assertFalse(structureComplete)
        
        @Suppress("UNCHECKED_CAST")
        val missingElements = state["missingElements"] as List<String>
        assertTrue(missingElements.isNotEmpty())
    }
    
    @Test
    fun testSwingPointDetection() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "swing_points_test",
            candles = createTestCandles(count = 20),
            detectedLines = listOf(
                TrendLine(x1 = 10.0, y1 = 20.0, x2 = 100.0, y2 = 80.0, confidence = 0.9)
            ),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        // Verify swing point count is tracked
        assertTrue(state.containsKey("swingPointCount"))
        val swingCount = state["swingPointCount"] as Int
        assertTrue(swingCount >= 0)
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
            detectedLines = listOf(
                TrendLine(x1 = 10.0, y1 = 20.0, x2 = 100.0, y2 = 80.0, confidence = 0.9)
            ),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        // Run multiple times
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
        assertEquals(verdict1.reason, verdict2.reason)
        assertEquals(verdict1.reason, verdict3.reason)
        
        // State should be identical
        assertEquals(state1["structureComplete"], state2["structureComplete"])
        assertEquals(state1["structureComplete"], state3["structureComplete"])
        assertEquals(state1["swingPointCount"], state2["swingPointCount"])
        assertEquals(state1["swingPointCount"], state3["swingPointCount"])
    }
    
    @Test
    fun testRequiredElementsTracking() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "elements_test",
            candles = createTestCandles(count = 20),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        @Suppress("UNCHECKED_CAST")
        val requiredElements = state["requiredElements"] as List<String>
        assertFalse(requiredElements.isEmpty())
        
        // Should contain expected element types
        assertTrue(requiredElements.contains("swingPoints") ||
                   requiredElements.contains("trendlines") ||
                   requiredElements.contains("priceAction"))
    }
}
