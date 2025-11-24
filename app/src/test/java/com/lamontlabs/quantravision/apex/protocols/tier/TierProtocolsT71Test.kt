package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T71ExoticVolatilityRejection
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TierProtocolsT71Test {
    
    private lateinit var protocol: T71ExoticVolatilityRejection
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T71ExoticVolatilityRejection()
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
    
    private fun createInvertedVolatilityCandles(): List<Candle> {
        val candles = mutableListOf<Candle>()
        for (i in 0 until 40) {
            val basePrice = 100.0
            val priceRange = if (i >= 25 && i < 30) 5.0 else 0.5
            val volume = if (i >= 25 && i < 30) 5000.0 else 15000.0
            
            candles.add(
                Candle(
                    timestamp = 1000000L + (i * 60000L),
                    open = basePrice,
                    high = basePrice + priceRange,
                    low = basePrice - priceRange,
                    close = basePrice + priceRange * 0.5,
                    volume = volume
                )
            )
        }
        return candles
    }
    
    @Test
    fun testProtocolId() {
        assertEquals("T71", protocol.protocolId)
        assertEquals("ExoticVolatilityRejection", protocol.protocolName)
        assertEquals(3.2, protocol.weight, 0.01)
    }
    
    @Test
    fun testInsufficientCandles() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(30)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("Insufficient candles"))
        assertEquals(true, state["exoticVolatilityDetected"])
        assertEquals(0.0, state["exoticRejectionScore"])
    }
    
    @Test
    fun testCleanVolatility() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(40)
        
        state["volatilityException"] = false
        state["volatilitySpikeDetected"] = false
        state["abnormalMovementFlag"] = false
        state["volumeAnomalyDetected"] = false
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertFalse(state["exoticVolatilityDetected"] as Boolean)
        assertEquals(1.0, state["exoticRejectionScore"])
    }
    
    @Test
    fun testVolatilityException() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(40)
        
        state["volatilityException"] = true
        state["volatilitySpikeDetected"] = false
        state["abnormalMovementFlag"] = false
        state["volumeAnomalyDetected"] = false
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertTrue(state["exoticVolatilityDetected"] as Boolean)
        assertEquals(0.0, state["exoticRejectionScore"])
    }
    
    @Test
    fun testAbnormalMovement() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(40)
        
        state["volatilityException"] = false
        state["volatilitySpikeDetected"] = false
        state["abnormalMovementFlag"] = true
        state["volumeAnomalyDetected"] = false
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertTrue(state["exoticVolatilityDetected"] as Boolean)
        assertEquals(0.0, state["exoticRejectionScore"])
    }
    
    @Test
    fun testInvertedPattern() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash",
            candles = createInvertedVolatilityCandles(),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        state["volatilityException"] = false
        state["volatilitySpikeDetected"] = false
        state["abnormalMovementFlag"] = false
        state["volumeAnomalyDetected"] = false
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertTrue(state["exoticVolatilityDetected"] as Boolean)
        assertEquals(0.0, state["exoticRejectionScore"])
    }
    
    @Test
    fun testFailClosedDefaults() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(40)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertFalse(state["exoticVolatilityDetected"] as Boolean)
    }
    
    @Test
    fun testDeterminism() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(40)
        
        val testState = mutableMapOf<String, Any>(
            "volatilityException" to false,
            "volatilitySpikeDetected" to false,
            "abnormalMovementFlag" to false,
            "volumeAnomalyDetected" to false
        )
        
        val verdict1 = protocol.evaluate(context, primitives, testState.toMutableMap())
        val verdict2 = protocol.evaluate(context, primitives, testState.toMutableMap())
        val verdict3 = protocol.evaluate(context, primitives, testState.toMutableMap())
        
        assertEquals(verdict1.passed, verdict2.passed)
        assertEquals(verdict1.passed, verdict3.passed)
        assertEquals(verdict1.confidence, verdict2.confidence, 0.0001)
        assertEquals(verdict1.confidence, verdict3.confidence, 0.0001)
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
