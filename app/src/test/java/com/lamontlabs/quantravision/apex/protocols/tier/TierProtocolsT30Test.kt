package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T30BreakoutValidation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TierProtocolsT30Test {
    
    private lateinit var protocol: T30BreakoutValidation
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T30BreakoutValidation()
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
        assertEquals("T30", protocol.protocolId)
        assertEquals("BreakoutValidation", protocol.protocolName)
        assertEquals(2.1, protocol.weight, 0.01)
    }
    
    @Test
    fun testInsufficientCandles() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(10)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("Insufficient candles"))
        assertEquals(false, state["breakoutCandidate"])
        assertEquals(0.0, state["breakoutScore"])
    }
    
    @Test
    fun testBreakoutDetected() = runBlocking {
        val context = createTestContext()
        
        val candles = mutableListOf<Candle>()
        for (i in 0 until 25) {
            val price = 100.0 + i * 0.5
            candles.add(
                Candle(
                    timestamp = 1000000L + (i * 60000L),
                    open = price,
                    high = price + 0.5,
                    low = price - 0.5,
                    close = price + 0.2,
                    volume = if (i >= 20) 30000.0 else 10000.0
                )
            )
        }
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash",
            candles = candles,
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        state["priceRange"] = 15.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(state["breakoutCandidate"] as Boolean)
        assertTrue((state["breakoutScore"] as Double) >= 0.6)
    }
    
    @Test
    fun testNoBreakout() = runBlocking {
        val context = createTestContext()
        val candles = createTestCandles(25, 100.0)
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash",
            candles = candles,
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
        
        state["priceRange"] = 5.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(state["breakoutCandidate"] as Boolean)
        assertTrue((state["breakoutScore"] as Double) < 0.6)
    }
    
    @Test
    fun testDeterminism() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(30)
        state["priceRange"] = 10.0
        
        val verdict1 = protocol.evaluate(context, primitives, mutableMapOf("priceRange" to 10.0))
        val verdict2 = protocol.evaluate(context, primitives, mutableMapOf("priceRange" to 10.0))
        val verdict3 = protocol.evaluate(context, primitives, mutableMapOf("priceRange" to 10.0))
        
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
            candles = createTestCandles(candleCount),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
    }
}
