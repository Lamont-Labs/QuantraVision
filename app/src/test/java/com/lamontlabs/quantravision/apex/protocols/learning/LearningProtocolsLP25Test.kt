package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.learning.LP25LearningStateFinalizer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LearningProtocolsLP25Test {
    private lateinit var protocol: LP25LearningStateFinalizer
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = LP25LearningStateFinalizer()
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
        assertEquals("LP25", protocol.protocolId)
        assertEquals("LearningStateFinalizer", protocol.protocolName)
    }
    
    @Test
    fun testValidInputWithAllLearningMarkers() = runBlocking {
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
        
        state["freshnessTokenValid"] = true
        state["learningProofDigest"] = "abc123def456789"
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(0.95, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("PASS"))
        
        assertEquals(true, state["learningStateReady"])
    }
    
    @Test
    fun testFailClosedWhenFreshnessTokenInvalid() = runBlocking {
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
        
        state["suppressionMemoryScore"] = 0.7
        state["driftAdaptationScore"] = 0.6
        state["patternEffectivenessScore"] = 0.8
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("Freshness token invalid"))
        
        assertEquals(false, state["learningStateReady"])
        
        assertEquals(0.0, state["suppressionMemoryScore"])
        assertEquals(0.0, state["driftAdaptationScore"])
        assertEquals(0.0, state["patternEffectivenessScore"])
        assertEquals(0.0, state["adaptiveConfidenceModifier"])
        assertEquals("", state["learningProofDigest"])
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
        
        val state1 = mutableMapOf<String, Any>(
            "freshnessTokenValid" to true,
            "learningProofDigest" to "xyz789abc123"
        )
        val state2 = mutableMapOf<String, Any>(
            "freshnessTokenValid" to true,
            "learningProofDigest" to "xyz789abc123"
        )
        val state3 = mutableMapOf<String, Any>(
            "freshnessTokenValid" to true,
            "learningProofDigest" to "xyz789abc123"
        )
        
        val verdict1 = protocol.evaluate(context, primitives, state1)
        val verdict2 = protocol.evaluate(context, primitives, state2)
        val verdict3 = protocol.evaluate(context, primitives, state3)
        
        assertEquals(verdict1.passed, verdict2.passed)
        assertEquals(verdict1.passed, verdict3.passed)
        assertEquals(verdict1.confidence, verdict2.confidence, 0.0001)
        assertEquals(verdict1.confidence, verdict3.confidence, 0.0001)
        assertEquals(verdict1.reason, verdict2.reason)
        assertEquals(verdict1.reason, verdict3.reason)
        
        assertEquals(state1["learningStateReady"], state2["learningStateReady"])
        assertEquals(state1["learningStateReady"], state3["learningStateReady"])
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
        
        state["freshnessTokenValid"] = true
        state["learningProofDigest"] = "test_digest_12345"
        
        protocol.evaluate(context, primitives, state)
        
        assertTrue(state.containsKey("learningStateReady"))
        assertEquals(true, state["learningStateReady"])
    }
    
    @Test
    fun testStateResetOnFailure() = runBlocking {
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
        
        state["freshnessTokenValid"] = false
        state["suppressionMemoryScore"] = 0.9
        state["driftAdaptationScore"] = 0.85
        state["patternEffectivenessScore"] = 0.92
        state["adaptiveConfidenceModifier"] = 0.88
        state["learningProofDigest"] = "should_be_cleared"
        
        protocol.evaluate(context, primitives, state)
        
        assertEquals(0.0, state["suppressionMemoryScore"])
        assertEquals(0.0, state["driftAdaptationScore"])
        assertEquals(0.0, state["patternEffectivenessScore"])
        assertEquals(0.0, state["adaptiveConfidenceModifier"])
        assertEquals("", state["learningProofDigest"])
    }
    
    @Test
    fun testLearningProofDigestIncludedInReason() = runBlocking {
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
        
        val testDigest = "1234567890abcdef"
        state["freshnessTokenValid"] = true
        state["learningProofDigest"] = testDigest
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.reason.contains(testDigest.take(16)))
    }
    
    @Test
    fun testMissingFreshnessToken() = runBlocking {
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
        
        state["learningProofDigest"] = "test_digest"
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(false, state["learningStateReady"])
    }
}
