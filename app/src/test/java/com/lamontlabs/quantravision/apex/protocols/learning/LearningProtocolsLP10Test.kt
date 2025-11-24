package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.learning.LP10DriftAdaptationAggregator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LearningProtocolsLP10Test {
    private lateinit var protocol: LP10DriftAdaptationAggregator
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = LP10DriftAdaptationAggregator()
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
        assertEquals("LP10", protocol.protocolId)
        assertEquals("DriftAdaptationAggregator", protocol.protocolName)
    }
    
    @Test
    fun testValidInputWithDriftData() = runBlocking {
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
        
        state["trendDriftCalculated"] = true
        state["regimeShiftScore"] = 0.7
        state["volatilityDriftMagnitude"] = 0.3
        state["trendDriftScore"] = 0.6
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(verdict.confidence > 0.4)
        assertTrue(verdict.reason.contains("PASS"))
        
        assertTrue(state.containsKey("driftAdaptationScore"))
        assertTrue(state.containsKey("driftTrendVector"))
        
        val score = state["driftAdaptationScore"] as Double
        val vector = state["driftTrendVector"] as String
        
        assertTrue(score > 0.0)
        assertNotNull(vector)
        assertFalse(vector.isEmpty())
    }
    
    @Test
    fun testFailClosedWhenTrendDriftNotCalculated() = runBlocking {
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
        assertTrue(verdict.reason.contains("Trend drift not calculated"))
        
        assertEquals(0.0, state["driftAdaptationScore"])
        assertEquals("", state["driftTrendVector"])
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
            "trendDriftCalculated" to true,
            "regimeShiftScore" to 0.65,
            "volatilityDriftMagnitude" to 0.4,
            "trendDriftScore" to 0.55
        )
        val state2 = mutableMapOf<String, Any>(
            "trendDriftCalculated" to true,
            "regimeShiftScore" to 0.65,
            "volatilityDriftMagnitude" to 0.4,
            "trendDriftScore" to 0.55
        )
        val state3 = mutableMapOf<String, Any>(
            "trendDriftCalculated" to true,
            "regimeShiftScore" to 0.65,
            "volatilityDriftMagnitude" to 0.4,
            "trendDriftScore" to 0.55
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
        
        assertEquals(state1["driftAdaptationScore"], state2["driftAdaptationScore"])
        assertEquals(state1["driftTrendVector"], state2["driftTrendVector"])
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
        
        state["trendDriftCalculated"] = true
        state["regimeShiftScore"] = 0.8
        state["volatilityDriftMagnitude"] = 0.2
        state["trendDriftScore"] = 0.7
        
        protocol.evaluate(context, primitives, state)
        
        assertTrue(state.containsKey("driftAdaptationScore"))
        assertTrue(state.containsKey("driftTrendVector"))
        
        val score = state["driftAdaptationScore"] as Double
        val vector = state["driftTrendVector"] as String
        
        assertTrue(score >= 0.0 && score <= 1.0)
        assertTrue(vector in listOf("STABLE_UPTREND", "STABLE_DOWNTREND", "HIGH_VOLATILITY", "NEUTRAL"))
    }
    
    @Test
    fun testDriftTrendVectorCalculation() = runBlocking {
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
        
        state["trendDriftCalculated"] = true
        state["regimeShiftScore"] = 0.5
        state["volatilityDriftMagnitude"] = 0.8
        state["trendDriftScore"] = 0.5
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        val vector = state["driftTrendVector"] as String
        assertEquals("HIGH_VOLATILITY", vector)
    }
    
    @Test
    fun testLowDriftAdaptationScore() = runBlocking {
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
        
        state["trendDriftCalculated"] = true
        state["regimeShiftScore"] = 0.2
        state["volatilityDriftMagnitude"] = 0.8
        state["trendDriftScore"] = 0.1
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        
        val score = state["driftAdaptationScore"] as Double
        assertTrue(score <= 0.4)
    }
}
