package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.models.*
import com.lamontlabs.quantravision.apex.protocols.tier.mobile.T79ProofLogSimilarityHooks
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TierProtocolsT79Test {
    
    private lateinit var protocol: T79ProofLogSimilarityHooks
    private lateinit var state: MutableMap<String, Any>
    
    @Before
    fun setUp() {
        protocol = T79ProofLogSimilarityHooks()
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
        assertEquals("T79", protocol.protocolId)
        assertEquals("ProofLogSimilarityHooks", protocol.protocolName)
        assertEquals(3.45, protocol.weight, 0.01)
    }
    
    @Test
    fun testInsufficientCandlesNeutral() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(25)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(0.5, verdict.confidence, 0.01)
        assertTrue(verdict.reason.contains("neutral score"))
        assertEquals(0.5, state["proofSimilarityScore"])
        assertEquals(true, state["proofReady"])
    }
    
    @Test
    fun testStubMode() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertEquals(0.5, state["proofSimilarityScore"])
        assertEquals(true, state["proofReady"])
    }
    
    @Test
    fun testProofFingerprintCreated() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(state.containsKey("proofFingerprint"))
        assertNotNull(state["proofFingerprint"])
        val fingerprint = state["proofFingerprint"] as String
        assertFalse(fingerprint.isEmpty())
    }
    
    @Test
    fun testProofHashCreated() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        state["testKey1"] = "value1"
        state["testKey2"] = 42
        state["testKey3"] = true
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(state.containsKey("proofHash"))
        assertNotNull(state["proofHash"])
        val hash = state["proofHash"] as String
        assertTrue(hash.length > 0)
    }
    
    @Test
    fun testDeterministicHash() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        val testState1 = mutableMapOf<String, Any>(
            "key1" to "value1",
            "key2" to 100
        )
        val testState2 = mutableMapOf<String, Any>(
            "key1" to "value1",
            "key2" to 100
        )
        
        protocol.evaluate(context, primitives, testState1)
        protocol.evaluate(context, primitives, testState2)
        
        val hash1 = testState1["proofHash"] as String
        val hash2 = testState2["proofHash"] as String
        
        assertEquals(hash1, hash2)
    }
    
    @Test
    fun testDifferentStatesDifferentHashes() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        val testState1 = mutableMapOf<String, Any>(
            "key1" to "value1"
        )
        val testState2 = mutableMapOf<String, Any>(
            "key1" to "value2"
        )
        
        protocol.evaluate(context, primitives, testState1)
        protocol.evaluate(context, primitives, testState2)
        
        val hash1 = testState1["proofHash"] as String
        val hash2 = testState2["proofHash"] as String
        
        assertNotEquals(hash1, hash2)
    }
    
    @Test
    fun testDifferentCandlesDifferentFingerprints() = runBlocking {
        val context = createTestContext()
        val primitives1 = createTestPrimitives(35, 100.0)
        val primitives2 = createTestPrimitives(35, 200.0)
        
        val testState1 = mutableMapOf<String, Any>()
        val testState2 = mutableMapOf<String, Any>()
        
        protocol.evaluate(context, primitives1, testState1)
        protocol.evaluate(context, primitives2, testState2)
        
        val fingerprint1 = testState1["proofFingerprint"] as String
        val fingerprint2 = testState2["proofFingerprint"] as String
        
        assertNotEquals(fingerprint1, fingerprint2)
    }
    
    @Test
    fun testAlwaysReady() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        val verdict = protocol.evaluate(context, primitives, state)
        
        assertTrue(verdict.passed)
        assertTrue(state["proofReady"] as Boolean)
    }
    
    @Test
    fun testDeterminism() = runBlocking {
        val context = createTestContext()
        val primitives = createTestPrimitives(35)
        
        val testState = mutableMapOf<String, Any>(
            "testKey" to "testValue"
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
    
    private fun createTestPrimitives(candleCount: Int, basePrice: Double = 100.0): ChartPrimitives {
        return ChartPrimitives(
            rawImageHash = "test_hash",
            candles = createTestCandles(candleCount, basePrice),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
    }
}
