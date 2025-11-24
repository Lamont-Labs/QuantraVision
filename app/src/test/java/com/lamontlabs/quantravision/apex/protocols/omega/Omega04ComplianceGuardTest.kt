package com.lamontlabs.quantravision.apex.protocols.omega

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.omega.Omega04ComplianceGuard
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class Omega04ComplianceGuardTest {
    
    private lateinit var protocol: Omega04ComplianceGuard
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = Omega04ComplianceGuard()
        state = mutableMapOf()
    }
    
    private fun createTestContext(tier: SubscriptionTier = SubscriptionTier.PRO): ApexScanContext {
        return ApexScanContext(
            ticker = "AAPL",
            timeframe = "1H",
            chartType = "candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test_user_123",
            tier = tier
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
    fun testPassWithValidCompliance() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 50)
        )
        
        state["disclaimerAcknowledged"] = true
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(1.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("PASS"))
        
        assertEquals(true, state["omega04_passed"])
        val violations = state["omega04_complianceViolations"] as List<*>
        assertTrue(violations.isEmpty())
    }
    
    @Test
    fun testFailWithoutDisclaimer() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 50)
        )
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        assertTrue(verdict.reason.contains("Disclaimer"))
        
        assertEquals(false, state["omega04_passed"])
        val violations = state["omega04_complianceViolations"] as List<*>
        assertTrue(violations.isNotEmpty())
    }
    
    @Test
    fun testFailWithExceededScanLimit() = runBlocking {
        val context = createTestContext(tier = SubscriptionTier.FREE)
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 50)
        )
        
        state["disclaimerAcknowledged"] = true
        state["dailyScanCount"] = 4
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        assertTrue(verdict.reason.contains("scan limit"))
        
        assertEquals(false, state["omega04_passed"])
        val violations = state["omega04_complianceViolations"] as List<*>
        assertTrue(violations.isNotEmpty())
    }
    
    @Test
    fun testFailWithExoticPatternOnFreeTier() = runBlocking {
        val context = createTestContext(tier = SubscriptionTier.FREE)
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 50)
        )
        
        state["disclaimerAcknowledged"] = true
        state["detectedPattern"] = "exotic_butterfly_pattern"
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        assertTrue(verdict.reason.contains("exotic"))
        
        assertEquals(false, state["omega04_passed"])
        val violations = state["omega04_complianceViolations"] as List<*>
        assertTrue(violations.isNotEmpty())
    }
    
    @Test
    fun testFailWithRestrictedCountry() = runBlocking {
        val context = createTestContext()
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 50)
        )
        
        state["disclaimerAcknowledged"] = true
        state["countryCode"] = "XX"
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("FAIL"))
        assertTrue(verdict.reason.contains("country"))
        
        assertEquals(false, state["omega04_passed"])
        val violations = state["omega04_complianceViolations"] as List<*>
        assertTrue(violations.isNotEmpty())
    }
    
    @Test
    fun testPassWithProTier() = runBlocking {
        val context = createTestContext(tier = SubscriptionTier.PRO)
        val primitives = ChartPrimitives(
            rawImageHash = "test_hash_123",
            candles = createTestCandles(count = 50)
        )
        
        state["disclaimerAcknowledged"] = true
        state["dailyScanCount"] = 10
        state["detectedPattern"] = "exotic_butterfly_pattern"
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(1.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("PASS"))
        
        assertEquals(true, state["omega04_passed"])
        val violations = state["omega04_complianceViolations"] as List<*>
        assertTrue(violations.isEmpty())
    }
}
