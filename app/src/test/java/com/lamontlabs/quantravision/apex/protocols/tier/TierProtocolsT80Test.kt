package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T80FinalVerdictFusion
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TierProtocolsT80Test {
    
    private lateinit var protocol: T80FinalVerdictFusion
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T80FinalVerdictFusion()
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
        assertEquals("T80", protocol.protocolId)
        assertEquals("FinalVerdictFusion", protocol.protocolName)
        assertEquals(3.5, protocol.weight, 0.01)
    }
    
    @Test
    fun testInsufficientCandles() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(25)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertEquals(0.0, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("Insufficient candles"))
        assertEquals(false, state["finalVerdict"])
        assertEquals(0.0, state["finalConfidence"])
        assertEquals(0.0, state["finalScore"])
    }
    
    @Test
    fun testAllGatesPass() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["preVerdictPassed"] = true
        state["normalizedScore"] = 85.0
        state["proofReady"] = true
        state["suppressionActive"] = false
        state["cascadingFailureRisk"] = 0.3
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(state["finalVerdict"] as Boolean)
        assertEquals(0.85, (state["finalConfidence"] as Double), 0.01)
        assertEquals(85.0, (state["finalScore"] as Double), 0.01)
    }
    
    @Test
    fun testPreVerdictFails() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["preVerdictPassed"] = false
        state["normalizedScore"] = 85.0
        state["proofReady"] = true
        state["suppressionActive"] = false
        state["cascadingFailureRisk"] = 0.3
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertFalse(state["finalVerdict"] as Boolean)
        assertEquals(0.0, state["finalConfidence"])
        assertEquals(0.0, state["finalScore"])
    }
    
    @Test
    fun testProofNotReady() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["preVerdictPassed"] = true
        state["normalizedScore"] = 85.0
        state["proofReady"] = false
        state["suppressionActive"] = false
        state["cascadingFailureRisk"] = 0.3
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertFalse(state["finalVerdict"] as Boolean)
    }
    
    @Test
    fun testSuppressionActive() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["preVerdictPassed"] = true
        state["normalizedScore"] = 85.0
        state["proofReady"] = true
        state["suppressionActive"] = true
        state["cascadingFailureRisk"] = 0.3
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertFalse(state["finalVerdict"] as Boolean)
    }
    
    @Test
    fun testCascadingFailureRisk() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["preVerdictPassed"] = true
        state["normalizedScore"] = 85.0
        state["proofReady"] = true
        state["suppressionActive"] = false
        state["cascadingFailureRisk"] = 0.6
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertFalse(state["finalVerdict"] as Boolean)
    }
    
    @Test
    fun testFailClosedDefaults() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertFalse(verdict.passed)
        assertFalse(state["finalVerdict"] as Boolean)
        assertEquals(0.0, state["finalConfidence"])
        assertEquals(0.0, state["finalScore"])
    }
    
    @Test
    fun testScoreMapping() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        val testScores = listOf(50.0, 75.0, 100.0)
        
        for (score in testScores) {
            val testState = mutableMapOf<String, Any>(
                "preVerdictPassed" to true,
                "normalizedScore" to score,
                "proofReady" to true,
                "suppressionActive" to false,
                "cascadingFailureRisk" to 0.3
            )
            
            val verdict = protocol.evaluate(context, primitives, testState)
            
            assertTrue(verdict.passed)
            assertEquals(score / 100.0, (testState["finalConfidence"] as Double), 0.01)
            assertEquals(score, (testState["finalScore"] as Double), 0.01)
        }
    }
    
    @Test
    fun testBoundaryCondition() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["preVerdictPassed"] = true
        state["normalizedScore"] = 85.0
        state["proofReady"] = true
        state["suppressionActive"] = false
        state["cascadingFailureRisk"] = 0.49
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(state["finalVerdict"] as Boolean)
    }
    
    @Test
    fun testDeterminism() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        val testState = mutableMapOf<String, Any>(
            "preVerdictPassed" to true,
            "normalizedScore" to 75.0,
            "proofReady" to true,
            "suppressionActive" to false,
            "cascadingFailureRisk" to 0.4
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
