package com.lamontlabs.quantravision.apex.protocols.omega

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.omega.Omega01StructuralAnomalyGuard
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class Omega01StructuralAnomalyGuardTest {
    
    private lateinit var protocol: Omega01StructuralAnomalyGuard
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = Omega01StructuralAnomalyGuard()
        state = mutableMapOf()
    }
    
    private fun createTestContext(): ApexScanContext {
        return ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = SubscriptionTier.PRO
        )
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
    fun testPassWithValidCandles() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 50)
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(1.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("PASS"))
        
        assertEquals(true, state["omega01_passed"])
        assertEquals(0, state["omega01_anomalyCount"])
        assertTrue((state["omega01_reason"] as String).contains("structurally valid"))
    }
    
    @Test
    fun testFailWithInsufficientCandles() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 5)
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        assertTrue(verdict.reason.contains("Insufficient candles"))
        
        assertEquals(false, state["omega01_passed"])
        assertEquals(1, state["omega01_anomalyCount"])
    }
    
    @Test
    fun testFailWithNegativePrice() = runBlocking {
        val context = createTestContext()
        val candles = createTestCandles(count = 20).toMutableList()
        candles[10] = candles[10].copy(close = -50.0)
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = candles
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        
        assertEquals(false, state["omega01_passed"])
        assertTrue((state["omega01_anomalyCount"] as Int) > 0)
    }
    
    @Test
    fun testFailWithNaNPrice() = runBlocking {
        val context = createTestContext()
        val candles = createTestCandles(count = 20).toMutableList()
        candles[5] = candles[5].copy(high = Double.NaN)
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = candles
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        
        assertEquals(false, state["omega01_passed"])
        assertTrue((state["omega01_anomalyCount"] as Int) > 0)
    }
    
    @Test
    fun testFailWithInvalidPriceRelationship() = runBlocking {
        val context = createTestContext()
        val candles = createTestCandles(count = 20).toMutableList()
        candles[8] = candles[8].copy(high = 50.0, low = 100.0)
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = candles
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        
        assertEquals(false, state["omega01_passed"])
        assertTrue((state["omega01_anomalyCount"] as Int) > 0)
    }
    
    @Test
    fun testFailWithNegativeVolume() = runBlocking {
        val context = createTestContext()
        val candles = createTestCandles(count = 20).toMutableList()
        candles[12] = candles[12].copy(volume = -1000.0)
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = candles
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        
        assertEquals(false, state["omega01_passed"])
        assertTrue((state["omega01_anomalyCount"] as Int) > 0)
    }
    
    @Test
    fun testFailWithDuplicateTimestamps() = runBlocking {
        val context = createTestContext()
        val candles = createTestCandles(count = 20).toMutableList()
        candles[7] = candles[7].copy(timestamp = candles[6].timestamp)
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = candles
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        
        assertEquals(false, state["omega01_passed"])
        assertTrue((state["omega01_anomalyCount"] as Int) > 0)
    }
    
    @Test
    fun testFailWithNonMonotonicTimestamps() = runBlocking {
        val context = createTestContext()
        val candles = createTestCandles(count = 20).toMutableList()
        candles[10] = candles[10].copy(timestamp = candles[9].timestamp - 1000L)
        
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = candles
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        
        assertEquals(false, state["omega01_passed"])
        assertTrue((state["omega01_anomalyCount"] as Int) > 0)
    }
}
