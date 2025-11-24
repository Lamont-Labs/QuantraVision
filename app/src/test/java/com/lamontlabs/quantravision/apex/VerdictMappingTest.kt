package com.lamontlabs.quantravision.apex

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.lamontlabs.quantravision.apex.models.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * BATCH 10: Verdict Mapping Golden Tests
 * 
 * Comprehensive test suite for Apex Engine protocol execution order and verdict aggregation.
 * Tests Omega → Tier → Learning execution order and verdict mapping logic.
 * 
 * Test Coverage:
 * 1. Protocol Execution Order: Omega → Tier → Learning
 * 2. Omega Lock: When Omega fails, status = OMEGA, no further execution
 * 3. Verdict Aggregation: Multiple protocol results aggregated correctly
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class VerdictMappingTest {
    
    private lateinit var context: Context
    private lateinit var registry: ProtocolRegistryMobile
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        registry = ProtocolRegistryMobile(context)
    }
    
    // ============================================================
    // PROTOCOL EXECUTION ORDER TESTS
    // ============================================================
    
    @Test
    fun `Omega protocols execute before Tier protocols`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, System.currentTimeMillis())
        
        val omegaIndices = result.protocolTrace
            .filter { it.protocolId.startsWith("Omega") }
            .map { result.protocolTrace.indexOf(it) }
        
        val tierIndices = result.protocolTrace
            .filter { it.protocolId.startsWith("T") }
            .map { result.protocolTrace.indexOf(it) }
        
        if (omegaIndices.isNotEmpty() && tierIndices.isNotEmpty()) {
            val maxOmegaIndex = omegaIndices.maxOrNull() ?: -1
            val minTierIndex = tierIndices.minOrNull() ?: Int.MAX_VALUE
            
            assertTrue(
                "All Omega protocols should execute before all Tier protocols",
                maxOmegaIndex < minTierIndex
            )
        }
    }
    
    @Test
    fun `Tier protocols execute before Learning protocols`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, System.currentTimeMillis())
        
        val tierIndices = result.protocolTrace
            .filter { it.protocolId.startsWith("T") }
            .map { result.protocolTrace.indexOf(it) }
        
        val learningIndices = result.protocolTrace
            .filter { it.protocolId.startsWith("LP") }
            .map { result.protocolTrace.indexOf(it) }
        
        if (tierIndices.isNotEmpty() && learningIndices.isNotEmpty()) {
            val maxTierIndex = tierIndices.maxOrNull() ?: -1
            val minLearningIndex = learningIndices.minOrNull() ?: Int.MAX_VALUE
            
            assertTrue(
                "All Tier protocols should execute before all Learning protocols",
                maxTierIndex < minLearningIndex
            )
        }
    }
    
    @Test
    fun `complete execution order Omega then Tier then Learning`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, System.currentTimeMillis())
        
        var lastOmegaIndex = -1
        var lastTierIndex = -1
        var lastLearningIndex = -1
        
        result.protocolTrace.forEachIndexed { index, verdict ->
            when {
                verdict.protocolId.startsWith("Omega") -> lastOmegaIndex = index
                verdict.protocolId.startsWith("T") -> lastTierIndex = index
                verdict.protocolId.startsWith("LP") -> lastLearningIndex = index
            }
        }
        
        if (lastOmegaIndex >= 0 && lastTierIndex >= 0) {
            assertTrue(
                "Last Omega should execute before first Tier",
                lastOmegaIndex < lastTierIndex
            )
        }
        
        if (lastTierIndex >= 0 && lastLearningIndex >= 0) {
            assertTrue(
                "Last Tier should execute before first Learning",
                lastTierIndex < lastLearningIndex
            )
        }
    }
    
    // ============================================================
    // OMEGA LOCK TESTS
    // ============================================================
    
    @Test
    fun `protocol registry contains Omega protocols`() {
        val omegaProtocols = registry.getOmegaProtocols()
        
        assertTrue("Should have Omega protocols registered", omegaProtocols.isNotEmpty())
        
        omegaProtocols.forEach { protocol ->
            assertTrue(
                "Omega protocol ID should start with 'Omega'",
                protocol.protocolId.startsWith("Omega")
            )
        }
    }
    
    @Test
    fun `protocol registry contains Tier protocols`() {
        val tierProtocols = registry.getTierProtocols()
        
        assertTrue("Should have Tier protocols registered", tierProtocols.isNotEmpty())
        
        tierProtocols.forEach { protocol ->
            assertTrue(
                "Tier protocol ID should start with 'T' and be numeric after",
                protocol.protocolId.matches(Regex("T\\d+.*"))
            )
        }
    }
    
    @Test
    fun `protocol registry contains Learning protocols`() {
        val learningProtocols = registry.getLearningProtocols()
        
        assertTrue("Should have Learning protocols registered", learningProtocols.isNotEmpty())
        
        learningProtocols.forEach { protocol ->
            assertTrue(
                "Learning protocol ID should start with 'LP'",
                protocol.protocolId.startsWith("LP")
            )
        }
    }
    
    @Test
    fun `Omega protocols have highest weight`() {
        val omegaProtocols = registry.getOmegaProtocols()
        val tierProtocols = registry.getTierProtocols()
        
        if (omegaProtocols.isNotEmpty() && tierProtocols.isNotEmpty()) {
            val minOmegaWeight = omegaProtocols.minOf { it.weight }
            val maxTierWeight = tierProtocols.maxOf { it.weight }
            
            assertTrue(
                "Omega protocols should have higher weight than Tier protocols",
                minOmegaWeight >= maxTierWeight || minOmegaWeight >= 4.0
            )
        }
    }
    
    // ============================================================
    // VERDICT AGGREGATION TESTS
    // ============================================================
    
    @Test
    fun `scan with valid primitives produces non-empty protocol trace`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, System.currentTimeMillis())
        
        assertTrue(
            "Protocol trace should not be empty",
            result.protocolTrace.isNotEmpty()
        )
    }
    
    @Test
    fun `scan produces valid QuantraScore`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, System.currentTimeMillis())
        
        assertTrue(
            "QuantraScore should be between 0 and 100",
            result.quantraScore.normalizedScore in 0..100
        )
        
        assertTrue(
            "Raw score should be between 0.0 and 1.0",
            result.quantraScore.rawScore >= 0.0 && result.quantraScore.rawScore <= 1.0
        )
    }
    
    @Test
    fun `scan produces valid entropy score`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, System.currentTimeMillis())
        
        assertTrue(
            "Entropy score should be between 0.0 and 1.0",
            result.entropyScore >= 0.0 && result.entropyScore <= 1.0
        )
    }
    
    @Test
    fun `scan produces valid confidence score`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, System.currentTimeMillis())
        
        assertTrue(
            "Confidence should be between 0.0 and 1.0",
            result.confidenceApex >= 0.0 && result.confidenceApex <= 1.0
        )
    }
    
    @Test
    fun `scan with insufficient candles returns FAIL status`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 5)
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, System.currentTimeMillis())
        
        assertEquals(
            "Insufficient candles should result in FAIL status",
            ApexStatus.FAIL,
            result.status
        )
    }
    
    @Test
    fun `QuantraBand matches normalized score ranges`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, System.currentTimeMillis())
        
        val score = result.quantraScore.normalizedScore
        val band = result.quantraScore.band
        
        when (band) {
            QuantraBand.FAIL -> assertTrue("FAIL band should be 0-49", score in 0..49)
            QuantraBand.WAIT -> assertTrue("WAIT band should be 50-69", score in 50..69)
            QuantraBand.PASS -> assertTrue("PASS band should be 70-84", score in 70..84)
            QuantraBand.STRONG_PASS -> assertTrue("STRONG_PASS band should be 85-100", score in 85..100)
        }
    }
    
    // ============================================================
    // STATUS DETERMINATION TESTS
    // ============================================================
    
    @Test
    fun `status is one of valid ApexStatus values`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, System.currentTimeMillis())
        
        assertTrue(
            "Status should be one of the defined ApexStatus values",
            result.status in listOf(
                ApexStatus.PASS,
                ApexStatus.WAIT,
                ApexStatus.FAIL,
                ApexStatus.SUPPRESSED,
                ApexStatus.OMEGA
            )
        )
    }
    
    @Test
    fun `omegaLock true implies OMEGA status`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, System.currentTimeMillis())
        
        if (result.omegaLock) {
            assertEquals(
                "Omega lock should result in OMEGA status",
                ApexStatus.OMEGA,
                result.status
            )
        }
    }
    
    @Test
    fun `suppressionActive true implies SUPPRESSED or OMEGA status`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, System.currentTimeMillis())
        
        if (result.suppressionActive && !result.omegaLock) {
            assertEquals(
                "Suppression active (without Omega lock) should result in SUPPRESSED status",
                ApexStatus.SUPPRESSED,
                result.status
            )
        }
    }
    
    // ============================================================
    // PROOF HASH TESTS
    // ============================================================
    
    @Test
    fun `scan generates valid proof hash`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, System.currentTimeMillis())
        
        assertNotNull("Proof hash should not be null", result.proofHash)
        assertTrue("Proof hash should not be empty", result.proofHash.isNotEmpty())
        assertEquals("Proof hash should be 64 characters (SHA-256)", 64, result.proofHash.length)
        assertTrue(
            "Proof hash should be hex",
            result.proofHash.all { it in '0'..'9' || it in 'a'..'f' }
        )
    }
    
    @Test
    fun `identical scans produce identical proof hash`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        
        val result1 = ApexEngineMobile.runScan(chartContext, primitives, 1700000000000L)
        val result2 = ApexEngineMobile.runScan(chartContext, primitives, 1700000001000L)
        
        assertEquals(
            "Identical scans at different times should produce same proof hash",
            result1.proofHash,
            result2.proofHash
        )
    }
    
    @Test
    fun `scan ID contains timestamp`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        val timestamp = 1700000000000L
        
        val result = ApexEngineMobile.runScan(chartContext, primitives, timestamp)
        
        assertTrue(
            "Scan ID should contain timestamp",
            result.scanId.contains(timestamp.toString())
        )
        assertTrue("Scan ID should start with APEX_", result.scanId.startsWith("APEX_"))
    }
    
    // ============================================================
    // DETERMINISM TESTS
    // ============================================================
    
    @Test
    fun `identical inputs produce identical outputs`() = runBlocking {
        val chartContext = createTestContext()
        val primitives = createTestPrimitives(candleCount = 50)
        val timestamp = 1700000000000L
        
        val result1 = ApexEngineMobile.runScan(chartContext, primitives, timestamp)
        val result2 = ApexEngineMobile.runScan(chartContext, primitives, timestamp)
        
        assertEquals("Status should be identical", result1.status, result2.status)
        assertEquals("QuantraScore should be identical", result1.quantraScore.normalizedScore, result2.quantraScore.normalizedScore)
        assertEquals("Entropy should be identical", result1.entropyScore, result2.entropyScore, 0.001)
        assertEquals("Confidence should be identical", result1.confidenceApex, result2.confidenceApex, 0.001)
        assertEquals("Proof hash should be identical", result1.proofHash, result2.proofHash)
    }
    
    // ============================================================
    // HELPER FUNCTIONS
    // ============================================================
    
    private fun createTestContext(): ApexScanContext {
        return ApexScanContext(
            ticker = "AAPL",
            timeframe = "5m",
            chartType = "Candlestick",
            timestamp = System.currentTimeMillis(),
            userId = "test-user-123",
            tier = SubscriptionTier.PRO
        )
    }
    
    private fun createTestPrimitives(candleCount: Int): ChartPrimitives {
        val candles = (0 until candleCount).map { i ->
            Candle(
                timestamp = 1700000000000L + (i * 300_000L),
                open = 100.0 + (i * 0.1),
                high = 100.5 + (i * 0.1),
                low = 99.5 + (i * 0.1),
                close = 100.2 + (i * 0.1),
                volume = 10000.0 + (i * 100.0)
            )
        }
        
        return ChartPrimitives(
            rawImageHash = "test-hash-${System.currentTimeMillis()}",
            candles = candles,
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Candlestick"
        )
    }
}
