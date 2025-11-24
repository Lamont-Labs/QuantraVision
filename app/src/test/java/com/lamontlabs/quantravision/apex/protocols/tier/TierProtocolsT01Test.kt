package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for T01: InputSanitization protocol.
 * 
 * Tests:
 * - Determinism: same inputs → same outputs
 * - State mutations: verify correct state updates
 * - Edge cases: null, empty, invalid data
 * - Score impacts: verify they match specification
 */
class TierProtocolsT01Test {
    
    private lateinit var protocol: T01InputSanitization
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T01InputSanitization()
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
        assertEquals("T01", protocol.protocolId)
        assertEquals("InputSanitization", protocol.protocolName)
    }
    
    @Test
    fun testValidInput() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 50),
            detectedLines = listOf(
                TrendLine(x1 = 10.0, y1 = 20.0, x2 = 100.0, y2 = 80.0, confidence = 0.9)
            ),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(1.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("PASS"))
        
        // Verify state mutations
        assertEquals(true, state["inputValid"])
        assertEquals("AAPL", state["ticker"])
        assertEquals("1H", state["timeframe"])
    }
    
    @Test
    fun testInvalidTicker() = runBlocking {
        val context = ApexScanContext(
            ticker = "", // Empty ticker
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 50),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertTrue(verdict.confidence < 0.5)
        assertTrue(verdict.reason.contains("FAIL"))
        assertEquals(false, state["inputValid"])
    }
    
    @Test
    fun testInsufficientCandles() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 5), // Less than minimum 10
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertTrue(verdict.reason.contains("candles"))
        assertEquals(false, state["inputValid"])
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
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 30),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        // Run multiple times
        val verdict1 = protocol.evaluate(context, primitives, mutableMapOf())
        val verdict2 = protocol.evaluate(context, primitives, mutableMapOf())
        val verdict3 = protocol.evaluate(context, primitives, mutableMapOf())
        
        // All results should be identical
        assertEquals(verdict1.passed, verdict2.passed)
        assertEquals(verdict1.passed, verdict3.passed)
        assertEquals(verdict1.confidence, verdict2.confidence, 0.0001)
        assertEquals(verdict1.confidence, verdict3.confidence, 0.0001)
        assertEquals(verdict1.reason, verdict2.reason)
        assertEquals(verdict1.reason, verdict3.reason)
    }
    
    @Test
    fun testNullTicker() = runBlocking {
        val context = ApexScanContext(
            ticker = null,
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(false, state["inputValid"])
    }
    
    @Test
    fun testInvalidTimeframe() = runBlocking {
        val context = ApexScanContext(
            ticker = "AAPL",
            timeframe = "INVALID",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertTrue(verdict.reason.contains("timeframe"))
        assertEquals(false, state["inputValid"])
    }
}
