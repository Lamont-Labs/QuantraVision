package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T66SectorTrendValidator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TierProtocolsT66Test {
    
    private lateinit var protocol: T66SectorTrendValidator
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T66SectorTrendValidator()
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
        assertEquals("T66", protocol.protocolId)
        assertEquals("SectorTrendValidator", protocol.protocolName)
        assertEquals(3.1, protocol.weight, 0.01)
    }
    
    @Test
    fun testInsufficientCandles() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(25, emptyList())
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("Insufficient candles"))
        assertEquals(false, state["sectorTrendValid"])
        assertEquals(0.0, state["sectorTrendScore"])
    }
    
    @Test
    fun testNoDetectedLines() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35, emptyList())
        
        state["sectorCompatible"] = true
        state["trendDirection"] = 1.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(state["sectorTrendValid"] as Boolean)
        assertEquals(0.6, (state["sectorTrendScore"] as Double), 0.01)
    }
    
    @Test
    fun testLinesMatchUptrend() = runBlocking {
        val context = createTestContext()
        val lines = listOf(
            DetectedLine(x1 = 0.0, y1 = 100.0, x2 = 10.0, y2 = 110.0, confidence = 0.9),
            DetectedLine(x1 = 0.0, y1 = 102.0, x2 = 10.0, y2 = 112.0, confidence = 0.85)
        )
        val primitives = createTestPrimitives(35, lines)
        
        state["sectorCompatible"] = true
        state["trendDirection"] = 1.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(state["sectorTrendValid"] as Boolean)
        assertEquals(0.85, (state["sectorTrendScore"] as Double), 0.01)
    }
    
    @Test
    fun testLinesMatchDowntrend() = runBlocking {
        val context = createTestContext()
        val lines = listOf(
            DetectedLine(x1 = 0.0, y1 = 110.0, x2 = 10.0, y2 = 100.0, confidence = 0.9),
            DetectedLine(x1 = 0.0, y1 = 112.0, x2 = 10.0, y2 = 102.0, confidence = 0.85)
        )
        val primitives = createTestPrimitives(35, lines)
        
        state["sectorCompatible"] = true
        state["trendDirection"] = -1.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(state["sectorTrendValid"] as Boolean)
        assertEquals(0.85, (state["sectorTrendScore"] as Double), 0.01)
    }
    
    @Test
    fun testLinesMismatchTrend() = runBlocking {
        val context = createTestContext()
        val lines = listOf(
            DetectedLine(x1 = 0.0, y1 = 100.0, x2 = 10.0, y2 = 110.0, confidence = 0.9)
        )
        val primitives = createTestPrimitives(35, lines)
        
        state["sectorCompatible"] = true
        state["trendDirection"] = -1.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(state["sectorTrendValid"] as Boolean)
        assertEquals(0.3, (state["sectorTrendScore"] as Double), 0.01)
    }
    
    @Test
    fun testSectorIncompatible() = runBlocking {
        val context = createTestContext()
        val lines = listOf(
            DetectedLine(x1 = 0.0, y1 = 100.0, x2 = 10.0, y2 = 110.0, confidence = 0.9)
        )
        val primitives = createTestPrimitives(35, lines)
        
        state["sectorCompatible"] = false
        state["trendDirection"] = 1.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(state["sectorTrendValid"] as Boolean)
        assertEquals(0.3, (state["sectorTrendScore"] as Double), 0.01)
    }
    
    @Test
    fun testDeterminism() = runBlocking {
        val context = createTestContext()
        val lines = listOf(
            DetectedLine(x1 = 0.0, y1 = 100.0, x2 = 10.0, y2 = 105.0, confidence = 0.8)
        )
        val primitives = createTestPrimitives(35, lines)
        
        val testState = mutableMapOf<String, Any>(
            "sectorCompatible" to true,
            "trendDirection" to 0.5
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
    
    private fun createTestPrimitives(candleCount: Int, lines: List<DetectedLine>): ChartPrimitives {
        return ChartPrimitives(
            rawImageHash = "test_hash",
            candles = createTestCandles(candleCount),
            detectedLines = lines,
            ocrText = "",
            chartType = "Candlestick"
        )
    }
}
