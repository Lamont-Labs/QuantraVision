package com.lamontlabs.quantravision.apex.protocols.omega

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.omega.Omega02RiskCapEnforcer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class Omega02RiskCapEnforcerTest {
    
    private lateinit var protocol: Omega02RiskCapEnforcer
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = Omega02RiskCapEnforcer()
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
    fun testFailWithNoRiskMetrics() {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 15)
        )
        
        val verdict = runBlocking { protocol.evaluate(context, primitives, state) }
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.001)
        assertTrue(verdict.reason.contains("Missing all risk metrics"))
        assertFalse(state["omega02_passed"] as Boolean)
        
        val violations = state["omega02_riskViolations"] as List<*>
        assertTrue(violations.isNotEmpty())
        assertTrue(violations[0].toString().contains("No risk metrics available"))
    }
    
    @Test
    fun testFailWithInsufficientCandles() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 5)
        )
        state["quantraScore"] = 75.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        assertTrue(verdict.reason.contains("Insufficient candles"))
        
        assertEquals(false, state["omega02_passed"])
    }
    
    @Test
    fun testPassWithValidQuantraScore() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20)
        )
        state["quantraScore"] = 75.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(1.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("PASS"))
        
        assertEquals(true, state["omega02_passed"])
        val violations = state["omega02_riskViolations"] as List<*>
        assertTrue(violations.isEmpty())
    }
    
    @Test
    fun testFailWithExcessiveQuantraScore() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20)
        )
        state["quantraScore"] = 150.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        
        assertEquals(false, state["omega02_passed"])
        val violations = state["omega02_riskViolations"] as List<*>
        assertTrue(violations.isNotEmpty())
        assertTrue(violations[0].toString().contains("QuantraScore exceeds limit"))
    }
    
    @Test
    fun testPassWithValidConfidence() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20)
        )
        state["confidence"] = 0.85
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(1.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("PASS"))
        
        assertEquals(true, state["omega02_passed"])
    }
    
    @Test
    fun testFailWithExcessiveConfidence() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20)
        )
        state["confidence"] = 1.5
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        
        assertEquals(false, state["omega02_passed"])
        val violations = state["omega02_riskViolations"] as List<*>
        assertTrue(violations.isNotEmpty())
        assertTrue(violations[0].toString().contains("Confidence exceeds limit"))
    }
    
    @Test
    fun testFailWithOverLeveragedConfidence() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20)
        )
        state["confidence"] = 0.97
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        
        assertEquals(false, state["omega02_passed"])
        val violations = state["omega02_riskViolations"] as List<*>
        assertTrue(violations.isNotEmpty())
        assertTrue(violations[0].toString().contains("Over-leveraged"))
    }
    
    @Test
    fun testPassWithValidPositionSize() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20)
        )
        state["positionSize"] = 5.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(1.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("PASS"))
        
        assertEquals(true, state["omega02_passed"])
    }
    
    @Test
    fun testFailWithExcessivePositionSize() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20)
        )
        state["positionSize"] = 15.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        
        assertEquals(false, state["omega02_passed"])
        val violations = state["omega02_riskViolations"] as List<*>
        assertTrue(violations.isNotEmpty())
        assertTrue(violations[0].toString().contains("Position size exceeds limit"))
    }
    
    @Test
    fun testPassWithMultipleValidMetrics() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20)
        )
        state["quantraScore"] = 80.0
        state["confidence"] = 0.75
        state["positionSize"] = 7.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(1.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("PASS"))
        
        assertEquals(true, state["omega02_passed"])
        val violations = state["omega02_riskViolations"] as List<*>
        assertTrue(violations.isEmpty())
    }
    
    @Test
    fun testFailWithMultipleViolations() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20)
        )
        state["quantraScore"] = 150.0
        state["confidence"] = 1.2
        state["positionSize"] = 15.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        
        assertEquals(false, state["omega02_passed"])
        val violations = state["omega02_riskViolations"] as List<*>
        assertEquals(3, violations.size)
    }
    
    @Test
    fun testPassWithOnlyQuantraScore() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20)
        )
        state["quantraScore"] = 70.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(1.0, verdict.confidence, 0.01)
        
        assertEquals(true, state["omega02_passed"])
    }
    
    @Test
    fun testPassWithOnlyConfidence() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20)
        )
        state["confidence"] = 0.80
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(1.0, verdict.confidence, 0.01)
        
        assertEquals(true, state["omega02_passed"])
    }
    
    @Test
    fun testPassWithOnlyPositionSize() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 20)
        )
        state["positionSize"] = 8.0
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(1.0, verdict.confidence, 0.01)
        
        assertEquals(true, state["omega02_passed"])
    }
}
