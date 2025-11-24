package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T35MarketCondition
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TierProtocolsT35Test {
    
    private lateinit var protocol: T35MarketCondition
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T35MarketCondition()
        state = mutableMapOf()
    }
    
    private fun createTrendingCandles(count: Int, basePrice: Double = 100.0): List<Candle> {
        return (0 until count).map { i ->
            val price = basePrice + (i * 1.0)
            Candle(
                timestamp = 1000000L + (i * 60000L),
                open = price,
                high = price + 0.5,
                low = price - 0.3,
                close = price + 0.2,
                volume = 10000.0
            )
        }
    }
    
    private fun createRangingCandles(count: Int, basePrice: Double = 100.0): List<Candle> {
        return (0 until count).map { i ->
            val price = basePrice + (if (i % 2 == 0) 0.2 else -0.2)
            Candle(
                timestamp = 1000000L + (i * 60000L),
                open = price,
                high = price + 0.3,
                low = price - 0.3,
                close = price,
                volume = 10000.0
            )
        }
    }
    
    private fun createVolatileCandles(count: Int, basePrice: Double = 100.0): List<Candle> {
        return (0 until count).map { i ->
            val price = basePrice + (if (i % 2 == 0) 5.0 else -5.0)
            Candle(
                timestamp = 1000000L + (i * 60000L),
                open = price,
                high = price + 3.0,
                low = price - 3.0,
                close = price + 1.0,
                volume = 10000.0
            )
        }
    }
    
    @Test
    fun testProtocolId() {
        assertEquals("T35", protocol.protocolId)
        assertEquals("MarketCondition", protocol.protocolName)
        assertEquals(2.3, protocol.weight, 0.01)
    }
    
    @Test
    fun testInsufficientCandles() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(10)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("Insufficient candles"))
        assertEquals("UNKNOWN", state["marketRegime"])
    }
    
    @Test
    fun testTrendingMarket() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash",
            candles = createTrendingCandles(25),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals("TRENDING", state["marketRegime"])
        assertTrue((state["marketConditionScore"] as Double) >= 0.5)
        assertTrue(verdict.confidence >= 0.5)
    }
    
    @Test
    fun testRangingMarket() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash",
            candles = createRangingCandles(25),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals("RANGING", state["marketRegime"])
        assertTrue((state["marketConditionScore"] as Double) >= 0.5)
    }
    
    @Test
    fun testVolatileMarket() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash",
            candles = createVolatileCandles(25),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertEquals("VOLATILE", state["marketRegime"])
        assertTrue((state["marketConditionScore"] as Double) < 0.5)
    }
    
    @Test
    fun testDeterminism() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash",
            candles = createTrendingCandles(25),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict1 = protocol.evaluate(context, primitives, mutableMapOf())
        val verdict2 = protocol.evaluate(context, primitives, mutableMapOf())
        val verdict3 = protocol.evaluate(context, primitives, mutableMapOf())
        
        assertEquals(verdict1.passed, verdict2.passed)
        assertEquals(verdict1.passed, verdict3.passed)
        assertEquals(verdict1.confidence, verdict2.confidence, 0.0001)
        assertEquals(verdict1.confidence, verdict3.confidence, 0.0001)
        assertEquals(verdict1.reason, verdict2.reason)
        assertEquals(verdict1.reason, verdict3.reason)
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
            candles = (0 until candleCount).map { i ->
                Candle(
                    timestamp = 1000000L + (i * 60000L),
                    open = 100.0 + (i * 0.1),
                    high = 100.5 + (i * 0.1),
                    low = 99.5 + (i * 0.1),
                    close = 100.2 + (i * 0.1),
                    volume = 10000.0
                )
            },
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
    }
}
