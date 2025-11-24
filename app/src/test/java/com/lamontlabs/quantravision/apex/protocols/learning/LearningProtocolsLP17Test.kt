package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.learning.LP17AdaptiveClampEnforcer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LearningProtocolsLP17Test {
    private lateinit var protocol: LP17AdaptiveClampEnforcer
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = LP17AdaptiveClampEnforcer()
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
        assertEquals("LP17", protocol.protocolId)
        assertEquals("AdaptiveClampEnforcer", protocol.protocolName)
    }
    
    @Test
    fun testValidInputWithAdaptiveModifiers() = runBlocking {
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
        
        state["confidenceModifierCalculated"] = true
        state["blendedConfidenceModifier"] = 0.65
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(0.75, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("PASS"))
        
        assertEquals(true, state["clampEnforced"])
        assertTrue(state.containsKey("clampedConfidenceModifier"))
        
        val clampedValue = state["clampedConfidenceModifier"] as Double
        assertTrue(clampedValue >= 0.1 && clampedValue <= 0.95)
    }
    
    @Test
    fun testFailClosedWhenModifierNotCalculated() = runBlocking {
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
        assertTrue(verdict.reason.contains("Confidence modifier not calculated"))
        
        assertEquals(false, state["clampEnforced"])
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
            "confidenceModifierCalculated" to true,
            "blendedConfidenceModifier" to 0.55
        )
        val state2 = mutableMapOf<String, Any>(
            "confidenceModifierCalculated" to true,
            "blendedConfidenceModifier" to 0.55
        )
        val state3 = mutableMapOf<String, Any>(
            "confidenceModifierCalculated" to true,
            "blendedConfidenceModifier" to 0.55
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
        
        assertEquals(state1["clampEnforced"], state2["clampEnforced"])
        assertEquals(state1["clampedConfidenceModifier"], state2["clampedConfidenceModifier"])
    }
    
    @Test
    fun testClampEnforcementLowerBound() = runBlocking {
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
        
        state["confidenceModifierCalculated"] = true
        state["blendedConfidenceModifier"] = -0.5
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        val clampedValue = state["clampedConfidenceModifier"] as Double
        assertEquals(0.1, clampedValue, 0.0001)
    }
    
    @Test
    fun testClampEnforcementUpperBound() = runBlocking {
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
        
        state["confidenceModifierCalculated"] = true
        state["blendedConfidenceModifier"] = 1.5
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        val clampedValue = state["clampedConfidenceModifier"] as Double
        assertEquals(0.95, clampedValue, 0.0001)
    }
    
    @Test
    fun testClampEnforcementWithinRange() = runBlocking {
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
        
        state["confidenceModifierCalculated"] = true
        state["blendedConfidenceModifier"] = 0.45
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        val clampedValue = state["clampedConfidenceModifier"] as Double
        assertEquals(0.45, clampedValue, 0.0001)
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
        
        state["confidenceModifierCalculated"] = true
        state["blendedConfidenceModifier"] = 0.75
        
        protocol.evaluate(context, primitives, state)
        
        assertTrue(state.containsKey("clampEnforced"))
        assertTrue(state.containsKey("clampedConfidenceModifier"))
        
        assertEquals(true, state["clampEnforced"])
        
        val clampedValue = state["clampedConfidenceModifier"] as Double
        assertTrue(clampedValue >= 0.1 && clampedValue <= 0.95)
    }
}
