package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.learning.LP01SuppressionMemoryLoader
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LearningProtocolsLP01Test {
    private lateinit var protocol: LP01SuppressionMemoryLoader
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = LP01SuppressionMemoryLoader()
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
        assertEquals("LP01", protocol.protocolId)
        assertEquals("SuppressionMemoryLoader", protocol.protocolName)
    }
    
    @Test
    fun testValidInputWithSufficientCandles() = runBlocking {
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
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertNotNull(verdict)
        assertEquals("LP01", verdict.protocolId)
        assertEquals("SuppressionMemoryLoader", verdict.protocolName)
        
        assertTrue(state.containsKey("suppressionHistoryLoaded"))
        assertTrue(state.containsKey("suppressionHistorySize"))
        
        val historySize = state["suppressionHistorySize"] as Int
        assertTrue(historySize >= 0)
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
            candles = createTestCandles(count = 5),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("Insufficient candles"))
        
        assertEquals(false, state["suppressionHistoryLoaded"])
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
        
        val state1 = mutableMapOf<String, Any>()
        val state2 = mutableMapOf<String, Any>()
        val state3 = mutableMapOf<String, Any>()
        
        val verdict1 = protocol.evaluate(context, primitives, state1)
        val verdict2 = protocol.evaluate(context, primitives, state2)
        val verdict3 = protocol.evaluate(context, primitives, state3)
        
        assertEquals(verdict1.passed, verdict2.passed)
        assertEquals(verdict1.passed, verdict3.passed)
        assertEquals(verdict1.confidence, verdict2.confidence, 0.0001)
        assertEquals(verdict1.confidence, verdict3.confidence, 0.0001)
        assertEquals(verdict1.reason, verdict2.reason)
        assertEquals(verdict1.reason, verdict3.reason)
        
        assertEquals(state1["suppressionHistoryLoaded"], state2["suppressionHistoryLoaded"])
        assertEquals(state1["suppressionHistorySize"], state2["suppressionHistorySize"])
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
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 50),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        assertTrue(state.isEmpty())
        
        protocol.evaluate(context, primitives, state)
        
        assertTrue(state.containsKey("suppressionHistoryLoaded"))
        assertTrue(state.containsKey("suppressionHistorySize"))
        
        val loaded = state["suppressionHistoryLoaded"] as Boolean
        val size = state["suppressionHistorySize"] as Int
        
        assertTrue(size >= 0)
        assertNotNull(loaded)
    }
    
    @Test
    fun testMinimumCandlesEdgeCase() = runBlocking {
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
            candles = createTestCandles(count = 10),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertNotNull(verdict)
        assertTrue(state.containsKey("suppressionHistoryLoaded"))
        assertTrue(state.containsKey("suppressionHistorySize"))
    }
}
