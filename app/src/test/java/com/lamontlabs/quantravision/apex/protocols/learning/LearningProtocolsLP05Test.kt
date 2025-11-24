package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.learning.LP05SuppressionStateWriter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LearningProtocolsLP05Test {
    private lateinit var protocol: LP05SuppressionStateWriter
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = LP05SuppressionStateWriter()
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
        assertEquals("LP05", protocol.protocolId)
        assertEquals("SuppressionStateWriter", protocol.protocolName)
    }
    
    @Test
    fun testValidInputWithUpstreamSuppressionData() = runBlocking {
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
        
        state["suppressionMemoryScore"] = 0.75
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(verdict.confidence > 0.5)
        assertTrue(verdict.reason.contains("PASS"))
        
        assertEquals(true, state["suppressionDecayApplied"])
        assertEquals(true, state["suppressionStateWritten"])
    }
    
    @Test
    fun testFailClosedWhenSuppressionMemoryScoreMissing() = runBlocking {
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
        
        assertFalse(verdict.passed)
        assertEquals(0.2, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("Invalid suppression memory score"))
        
        assertEquals(false, state["suppressionDecayApplied"])
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
        
        val state1 = mutableMapOf<String, Any>("suppressionMemoryScore" to 0.65)
        val state2 = mutableMapOf<String, Any>("suppressionMemoryScore" to 0.65)
        val state3 = mutableMapOf<String, Any>("suppressionMemoryScore" to 0.65)
        
        val verdict1 = protocol.evaluate(context, primitives, state1)
        val verdict2 = protocol.evaluate(context, primitives, state2)
        val verdict3 = protocol.evaluate(context, primitives, state3)
        
        assertEquals(verdict1.passed, verdict2.passed)
        assertEquals(verdict1.passed, verdict3.passed)
        assertEquals(verdict1.confidence, verdict2.confidence, 0.0001)
        assertEquals(verdict1.confidence, verdict3.confidence, 0.0001)
        assertEquals(verdict1.reason, verdict2.reason)
        assertEquals(verdict1.reason, verdict3.reason)
        
        assertEquals(state1["suppressionDecayApplied"], state2["suppressionDecayApplied"])
        assertEquals(state1["suppressionStateWritten"], state2["suppressionStateWritten"])
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
        
        state["suppressionMemoryScore"] = 0.80
        
        protocol.evaluate(context, primitives, state)
        
        assertTrue(state.containsKey("suppressionDecayApplied"))
        assertTrue(state.containsKey("suppressionStateWritten"))
        
        assertEquals(true, state["suppressionDecayApplied"])
        assertEquals(true, state["suppressionStateWritten"])
    }
    
    @Test
    fun testZeroSuppressionMemoryScore() = runBlocking {
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
        
        state["suppressionMemoryScore"] = 0.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertTrue(verdict.reason.contains("FAIL"))
        assertEquals(false, state["suppressionDecayApplied"])
    }
    
    @Test
    fun testNegativeSuppressionMemoryScore() = runBlocking {
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
        
        state["suppressionMemoryScore"] = -0.5
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(false, state["suppressionDecayApplied"])
    }
}
