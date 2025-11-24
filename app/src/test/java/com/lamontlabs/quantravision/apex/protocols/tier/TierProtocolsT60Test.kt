package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T60MarketStressIndicator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TierProtocolsT60Test {
    
    private lateinit var protocol: T60MarketStressIndicator
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T60MarketStressIndicator()
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
        assertEquals("T60", protocol.protocolId)
        assertEquals("MarketStressIndicator", protocol.protocolName)
        assertEquals(3.0, protocol.weight, 0.01)
    }
    
    @Test
    fun testInsufficientCandles() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(25)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("Insufficient candles"))
        assertEquals(1.0, state["marketStressLevel"])
        assertEquals(false, state["volatilityGuardOk"])
    }
    
    @Test
    fun testLowMarketStress() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["volatilityException"] = false
        state["volatilitySpikeDetected"] = false
        state["abnormalMovementFlag"] = false
        state["volumeAnomalyDetected"] = false
        state["regimeShiftDetected"] = false
        state["volatilityExceptionScore"] = 0.25
        state["spikeIntensity"] = 0.2
        state["movementAbnormalityScore"] = 0.2
        state["volumeAnomalyScore"] = 0.2
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(state["volatilityGuardOk"] as Boolean)
        assertTrue((state["marketStressLevel"] as Double) < 0.7)
    }
    
    @Test
    fun testHighMarketStress() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["volatilityException"] = true
        state["volatilitySpikeDetected"] = true
        state["abnormalMovementFlag"] = true
        state["volumeAnomalyDetected"] = true
        state["regimeShiftDetected"] = false
        state["volatilityExceptionScore"] = 0.85
        state["spikeIntensity"] = 0.8
        state["movementAbnormalityScore"] = 0.85
        state["volumeAnomalyScore"] = 0.9
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertFalse(state["volatilityGuardOk"] as Boolean)
        assertTrue((state["marketStressLevel"] as Double) >= 0.7)
    }
    
    @Test
    fun testModerateMarketStress() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["volatilityException"] = false
        state["volatilitySpikeDetected"] = true
        state["abnormalMovementFlag"] = true
        state["volumeAnomalyDetected"] = false
        state["regimeShiftDetected"] = false
        state["volatilityExceptionScore"] = 0.4
        state["spikeIntensity"] = 0.6
        state["movementAbnormalityScore"] = 0.65
        state["volumeAnomalyScore"] = 0.35
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        val marketStressLevel = state["marketStressLevel"] as Double
        assertTrue(marketStressLevel >= 0.4)
    }
    
    @Test
    fun testAllExceptions() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["volatilityException"] = true
        state["volatilitySpikeDetected"] = true
        state["abnormalMovementFlag"] = true
        state["volumeAnomalyDetected"] = true
        state["regimeShiftDetected"] = true
        state["volatilityExceptionScore"] = 0.95
        state["spikeIntensity"] = 0.9
        state["movementAbnormalityScore"] = 0.95
        state["volumeAnomalyScore"] = 0.95
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertFalse(state["volatilityGuardOk"] as Boolean)
        assertEquals(0.95, (state["marketStressLevel"] as Double), 0.01)
    }
    
    @Test
    fun testDeterminism() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        val testState = mutableMapOf<String, Any>(
            "volatilityException" to true,
            "volatilitySpikeDetected" to false,
            "abnormalMovementFlag" to true,
            "volumeAnomalyDetected" to false,
            "regimeShiftDetected" to false,
            "volatilityExceptionScore" to 0.70,
            "spikeIntensity" to 0.3,
            "movementAbnormalityScore" to 0.75,
            "volumeAnomalyScore" to 0.4
        )
        
        val verdict1 = protocol.evaluate(context, primitives, testState.toMutableMap())
        val verdict2 = protocol.evaluate(context, primitives, testState.toMutableMap())
        val verdict3 = protocol.evaluate(context, primitives, testState.toMutableMap())
        
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
