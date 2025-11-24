package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T40MultiFrameEntropy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TierProtocolsT40Test {
    
    private lateinit var protocol: T40MultiFrameEntropy
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T40MultiFrameEntropy()
        state = mutableMapOf()
    }
    
    private fun createStableCandles(count: Int, basePrice: Double = 100.0): List<Candle> {
        return (0 until count).map { i ->
            val price = basePrice + (i * 0.05)
            Candle(
                timestamp = 1000000L + (i * 60000L),
                open = price,
                high = price + 0.1,
                low = price - 0.1,
                close = price + 0.02,
                volume = 10000.0
            )
        }
    }
    
    private fun createChaoticCandles(count: Int, basePrice: Double = 100.0): List<Candle> {
        return (0 until count).map { i ->
            val volatility = if (i % 3 == 0) 5.0 else if (i % 3 == 1) -3.0 else 2.0
            val price = basePrice + volatility
            Candle(
                timestamp = 1000000L + (i * 60000L),
                open = price,
                high = price + 2.0,
                low = price - 2.0,
                close = price + 0.5,
                volume = 10000.0
            )
        }
    }
    
    @Test
    fun testProtocolId() {
        assertEquals("T40", protocol.protocolId)
        assertEquals("MultiFrameEntropy", protocol.protocolName)
        assertEquals(2.4, protocol.weight, 0.01)
    }
    
    @Test
    fun testInsufficientCandles() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(20)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("Insufficient candles"))
        assertEquals(1.0, state["multiFrameEntropy"])
        assertEquals(false, state["multiFrameEntropyOk"])
    }
    
    @Test
    fun testLowEntropyStableMarket() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash",
            candles = createStableCandles(40),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue((state["multiFrameEntropy"] as Double) < 0.6)
        assertEquals(true, state["multiFrameEntropyOk"])
        assertTrue(verdict.confidence >= 0.4)
    }
    
    @Test
    fun testHighEntropyChaoticMarket() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash",
            candles = createChaoticCandles(40),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        val entropy = state["multiFrameEntropy"] as Double
        assertTrue(entropy >= 0.3)
        
        if (entropy >= 0.6) {
            assertFalse(verdict.passed)
            assertEquals(false, state["multiFrameEntropyOk"])
        }
    }
    
    @Test
    fun testDeterminism() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash",
            candles = createStableCandles(35),
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
    
    @Test
    fun testStateWrites() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        protocol.evaluate(context, primitives, state)
        
        assertTrue(state.containsKey("multiFrameEntropy"))
        assertTrue(state.containsKey("multiFrameEntropyOk"))
        
        val entropy = state["multiFrameEntropy"] as Double
        assertTrue(entropy >= 0.0 && entropy <= 1.0)
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
