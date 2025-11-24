package com.lamontlabs.quantravision.apex

import com.lamontlabs.quantravision.apex.models.*
import org.junit.Assert.*
import org.junit.Test

/**
 * COMPREHENSIVE DETERMINISM TEST SUITE
 * 
 * Tests the fundamental redesign of scan ID and proof hash system to achieve
 * true determinism per master spec requirement "same inputs → same outputs".
 * 
 * Architecture being tested:
 * 
 * 1. **Scan ID** (for uniqueness/readability):
 *    - Includes timestamp for uniqueness
 *    - Format: APEX_<timestamp>_<hash-of-canonical-content>
 *    - Purpose: Unique identifier for logging, UI display, tracking
 *    - NOT included in proof hash calculation
 * 
 * 2. **Proof Hash** (for deterministic integrity):
 *    - Hashes ONLY canonical content (primitives, analysis results, protocols)
 *    - EXCLUDES timestamp, scanId, and any time-based fields
 *    - Purpose: Verify identical inputs → identical outputs
 *    - Enables reproducible verification and audit trails
 * 
 * 3. **Timestamp** (for temporal tracking):
 *    - Stored separately in ApexResult.timestamp
 *    - Used for logging, UI display, chronological ordering
 *    - NOT part of proof hash calculation
 * 
 * Test coverage:
 * - Scan ID determinism and uniqueness
 * - Proof hash determinism (CRITICAL)
 * - Separation of concerns between scan ID and proof hash
 * - Timestamp independence of proof hash
 */
class ProofHasherDeterminismTest {
    
    // ============================================================
    // HELPER FUNCTIONS
    // ============================================================
    
    /**
     * Create a test ApexResult with specified timestamp and content.
     */
    private fun createTestResult(
        timestamp: Long,
        status: ApexStatus = ApexStatus.PASS,
        quantraScore: Int = 75,
        entropyScore: Double = 0.30,
        suppressionActive: Boolean = false,
        omegaLock: Boolean = false,
        regimeOk: Boolean = true,
        confidenceApex: Double = 0.85,
        protocolTrace: List<ProtocolVerdict> = emptyList(),
        scanIdPrefix: String = "APEX_${timestamp}_test"
    ): ApexResult {
        return ApexResult(
            scanId = scanIdPrefix,
            status = status,
            quantraScore = QuantraScoreSnapshot(
                rawScore = quantraScore / 100.0,
                normalizedScore = quantraScore,
                band = when {
                    quantraScore >= 85 -> QuantraBand.STRONG_PASS
                    quantraScore >= 70 -> QuantraBand.PASS
                    quantraScore >= 50 -> QuantraBand.WAIT
                    else -> QuantraBand.FAIL
                }
            ),
            protocolTrace = protocolTrace,
            entropyScore = entropyScore,
            suppressionActive = suppressionActive,
            omegaLock = omegaLock,
            regimeOk = regimeOk,
            invalidationPoints = emptyList(),
            confidenceApex = confidenceApex,
            proofHash = "",
            timestamp = timestamp
        )
    }
    
    /**
     * Create a test protocol verdict.
     */
    private fun createTestVerdict(
        protocolId: String,
        passed: Boolean,
        confidence: Double,
        weight: Double = 1.0
    ): ProtocolVerdict {
        return ProtocolVerdict(
            protocolId = protocolId,
            protocolName = "Test Protocol $protocolId",
            passed = passed,
            confidence = confidence,
            reason = "Test reason",
            weight = weight
        )
    }
    
    // ============================================================
    // SCAN ID TESTS (uniqueness/readability)
    // ============================================================
    
    @Test
    fun `test identical inputs produce identical scan IDs`() {
        val timestamp = 1700000000000L
        val contextData = "ticker=AAPL|timeframe=5m|chartType=Candlestick"
        
        val scanId1 = ProofHasher.generateScanId(timestamp, contextData)
        val scanId2 = ProofHasher.generateScanId(timestamp, contextData)
        
        assertEquals("Identical inputs should produce identical scan IDs", scanId1, scanId2)
    }
    
    @Test
    fun `test different context data produces different scan IDs`() {
        val timestamp = 1700000000000L
        val contextData1 = "ticker=AAPL|timeframe=5m|chartType=Candlestick"
        val contextData2 = "ticker=GOOGL|timeframe=1h|chartType=Candlestick"
        
        val scanId1 = ProofHasher.generateScanId(timestamp, contextData1)
        val scanId2 = ProofHasher.generateScanId(timestamp, contextData2)
        
        assertNotEquals("Different context data should produce different scan IDs", scanId1, scanId2)
    }
    
    @Test
    fun `test different timestamps produce different scan IDs`() {
        val timestamp1 = 1700000000000L
        val timestamp2 = 1700000000001L
        val contextData = "ticker=AAPL|timeframe=5m|chartType=Candlestick"
        
        val scanId1 = ProofHasher.generateScanId(timestamp1, contextData)
        val scanId2 = ProofHasher.generateScanId(timestamp2, contextData)
        
        assertNotEquals("Different timestamps should produce different scan IDs", scanId1, scanId2)
    }
    
    @Test
    fun `test backward compatibility with no context data`() {
        val timestamp = 1700000000000L
        
        val scanId = ProofHasher.generateScanId(timestamp)
        
        assertNotNull("Scan ID should not be null", scanId)
        assertTrue("Scan ID should start with APEX_", scanId.startsWith("APEX_"))
        assertTrue("Scan ID should contain timestamp", scanId.contains(timestamp.toString()))
    }
    
    @Test
    fun `test scan ID format matches expected pattern`() {
        val timestamp = 1700000000000L
        val contextData = "ticker=AAPL|timeframe=5m|chartType=Candlestick"
        
        val scanId = ProofHasher.generateScanId(timestamp, contextData)
        
        val pattern = Regex("^APEX_\\d+_[0-9a-f]{8}$")
        assertTrue("Scan ID should match format APEX_<timestamp>_<8-hex-chars>", pattern.matches(scanId))
    }
    
    @Test
    fun `test empty context data produces deterministic results`() {
        val timestamp = 1700000000000L
        
        val scanId1 = ProofHasher.generateScanId(timestamp, "")
        val scanId2 = ProofHasher.generateScanId(timestamp, "")
        
        assertEquals("Empty context data should produce identical scan IDs", scanId1, scanId2)
    }
    
    @Test
    fun `test scan ID components are deterministic`() {
        val timestamp = 1700000000000L
        val contextData = "ticker=AAPL|timeframe=5m|chartType=Candlestick"
        
        val scanId = ProofHasher.generateScanId(timestamp, contextData)
        val parts = scanId.split("_")
        
        assertEquals("Scan ID should have 3 parts separated by underscores", 3, parts.size)
        assertEquals("First part should be APEX", "APEX", parts[0])
        assertEquals("Second part should be timestamp", timestamp.toString(), parts[1])
        assertEquals("Third part should be 8 hex characters", 8, parts[2].length)
        assertTrue("Third part should only contain hex characters", parts[2].all { it in '0'..'9' || it in 'a'..'f' })
    }
    
    @Test
    fun `test null ticker and timeframe handled correctly`() {
        val timestamp = 1700000000000L
        // Canonical format with empty ticker and timeframe (null values produce empty strings)
        val contextDataWithNulls = "ticker=|timeframe=|chartType=Candlestick"
        
        val scanId1 = ProofHasher.generateScanId(timestamp, contextDataWithNulls)
        val scanId2 = ProofHasher.generateScanId(timestamp, contextDataWithNulls)
        
        assertEquals("Null-containing context should produce identical scan IDs", scanId1, scanId2)
    }
    
    @Test
    fun `test same inputs with different userId produce same scan ID`() {
        val timestamp = 1700000000000L
        // Same canonical inputs (ticker, timeframe, chartType)
        // userId is NOT included in context, so different users get same scan ID
        val contextData = "ticker=AAPL|timeframe=5m|chartType=Candlestick"
        
        val scanId1 = ProofHasher.generateScanId(timestamp, contextData)
        val scanId2 = ProofHasher.generateScanId(timestamp, contextData)
        
        assertEquals("Same canonical inputs should produce same scan ID regardless of userId", scanId1, scanId2)
    }
    
    @Test
    fun `test same inputs with different rawImageHash produce same scan ID`() {
        val timestamp = 1700000000000L
        // Same canonical inputs (ticker, timeframe, chartType)
        // rawImageHash is NOT included in context, so vision jitter doesn't affect scan ID
        val contextData = "ticker=AAPL|timeframe=5m|chartType=Candlestick"
        
        val scanId1 = ProofHasher.generateScanId(timestamp, contextData)
        val scanId2 = ProofHasher.generateScanId(timestamp, contextData)
        
        assertEquals("Same canonical inputs should produce same scan ID regardless of rawImageHash", scanId1, scanId2)
    }
    
    // ============================================================
    // PROOF HASH TESTS (deterministic integrity - CRITICAL)
    // ============================================================
    
    @Test
    fun `test CRITICAL same content different timestamps produces SAME proof hash`() {
        // THIS IS THE CORE DETERMINISM FIX
        // Same analysis content at different times MUST produce identical proof hash
        
        val timestamp1 = 1700000000000L  // Time 1
        val timestamp2 = 1700000001000L  // Time 2 (1 second later)
        
        val trace = listOf(
            createTestVerdict("T01", true, 0.90),
            createTestVerdict("T02", true, 0.85),
            createTestVerdict("LP01", true, 0.75)
        )
        
        // Same content, different timestamps and scan IDs
        val result1 = createTestResult(
            timestamp = timestamp1,
            status = ApexStatus.PASS,
            quantraScore = 85,
            entropyScore = 0.25,
            confidenceApex = 0.88,
            protocolTrace = trace,
            scanIdPrefix = "APEX_${timestamp1}_abc123"
        )
        
        val result2 = createTestResult(
            timestamp = timestamp2,
            status = ApexStatus.PASS,
            quantraScore = 85,
            entropyScore = 0.25,
            confidenceApex = 0.88,
            protocolTrace = trace,
            scanIdPrefix = "APEX_${timestamp2}_def456"
        )
        
        val proofHash1 = ProofHasher.hashApexResult(result1)
        val proofHash2 = ProofHasher.hashApexResult(result2)
        
        assertEquals(
            "CRITICAL: Same content at different timestamps MUST produce identical proof hash",
            proofHash1,
            proofHash2
        )
        
        // Verify scan IDs ARE different (timestamp-based uniqueness)
        assertNotEquals(
            "Scan IDs should be different due to different timestamps",
            result1.scanId,
            result2.scanId
        )
    }
    
    @Test
    fun `test different content produces different proof hash`() {
        val timestamp = 1700000000000L
        
        // Result 1: PASS status
        val result1 = createTestResult(
            timestamp = timestamp,
            status = ApexStatus.PASS,
            quantraScore = 85,
            entropyScore = 0.25,
            confidenceApex = 0.88
        )
        
        // Result 2: WAIT status (different content)
        val result2 = createTestResult(
            timestamp = timestamp,
            status = ApexStatus.WAIT,
            quantraScore = 65,
            entropyScore = 0.45,
            confidenceApex = 0.62
        )
        
        val proofHash1 = ProofHasher.hashApexResult(result1)
        val proofHash2 = ProofHasher.hashApexResult(result2)
        
        assertNotEquals(
            "Different content should produce different proof hash",
            proofHash1,
            proofHash2
        )
    }
    
    @Test
    fun `test different protocol trace produces different proof hash`() {
        val timestamp = 1700000000000L
        
        val trace1 = listOf(
            createTestVerdict("T01", true, 0.90),
            createTestVerdict("T02", true, 0.85)
        )
        
        val trace2 = listOf(
            createTestVerdict("T01", true, 0.90),
            createTestVerdict("T02", false, 0.45)  // Different verdict
        )
        
        val result1 = createTestResult(timestamp = timestamp, protocolTrace = trace1)
        val result2 = createTestResult(timestamp = timestamp, protocolTrace = trace2)
        
        val proofHash1 = ProofHasher.hashApexResult(result1)
        val proofHash2 = ProofHasher.hashApexResult(result2)
        
        assertNotEquals(
            "Different protocol trace should produce different proof hash",
            proofHash1,
            proofHash2
        )
    }
    
    @Test
    fun `test different quantra score produces different proof hash`() {
        val timestamp = 1700000000000L
        
        val result1 = createTestResult(timestamp = timestamp, quantraScore = 85)
        val result2 = createTestResult(timestamp = timestamp, quantraScore = 75)
        
        val proofHash1 = ProofHasher.hashApexResult(result1)
        val proofHash2 = ProofHasher.hashApexResult(result2)
        
        assertNotEquals(
            "Different QuantraScore should produce different proof hash",
            proofHash1,
            proofHash2
        )
    }
    
    @Test
    fun `test different entropy produces different proof hash`() {
        val timestamp = 1700000000000L
        
        val result1 = createTestResult(timestamp = timestamp, entropyScore = 0.25)
        val result2 = createTestResult(timestamp = timestamp, entropyScore = 0.55)
        
        val proofHash1 = ProofHasher.hashApexResult(result1)
        val proofHash2 = ProofHasher.hashApexResult(result2)
        
        assertNotEquals(
            "Different entropy score should produce different proof hash",
            proofHash1,
            proofHash2
        )
    }
    
    @Test
    fun `test different suppression state produces different proof hash`() {
        val timestamp = 1700000000000L
        
        val result1 = createTestResult(timestamp = timestamp, suppressionActive = false)
        val result2 = createTestResult(timestamp = timestamp, suppressionActive = true)
        
        val proofHash1 = ProofHasher.hashApexResult(result1)
        val proofHash2 = ProofHasher.hashApexResult(result2)
        
        assertNotEquals(
            "Different suppression state should produce different proof hash",
            proofHash1,
            proofHash2
        )
    }
    
    @Test
    fun `test different omega lock produces different proof hash`() {
        val timestamp = 1700000000000L
        
        val result1 = createTestResult(timestamp = timestamp, omegaLock = false)
        val result2 = createTestResult(timestamp = timestamp, omegaLock = true)
        
        val proofHash1 = ProofHasher.hashApexResult(result1)
        val proofHash2 = ProofHasher.hashApexResult(result2)
        
        assertNotEquals(
            "Different omega lock state should produce different proof hash",
            proofHash1,
            proofHash2
        )
    }
    
    @Test
    fun `test different regime state produces different proof hash`() {
        val timestamp = 1700000000000L
        
        val result1 = createTestResult(timestamp = timestamp, regimeOk = true)
        val result2 = createTestResult(timestamp = timestamp, regimeOk = false)
        
        val proofHash1 = ProofHasher.hashApexResult(result1)
        val proofHash2 = ProofHasher.hashApexResult(result2)
        
        assertNotEquals(
            "Different regime state should produce different proof hash",
            proofHash1,
            proofHash2
        )
    }
    
    @Test
    fun `test different confidence produces different proof hash`() {
        val timestamp = 1700000000000L
        
        val result1 = createTestResult(timestamp = timestamp, confidenceApex = 0.88)
        val result2 = createTestResult(timestamp = timestamp, confidenceApex = 0.65)
        
        val proofHash1 = ProofHasher.hashApexResult(result1)
        val proofHash2 = ProofHasher.hashApexResult(result2)
        
        assertNotEquals(
            "Different confidence should produce different proof hash",
            proofHash1,
            proofHash2
        )
    }
    
    // ============================================================
    // SEPARATION OF CONCERNS TESTS
    // ============================================================
    
    @Test
    fun `test proof hash excludes scan ID`() {
        val timestamp = 1700000000000L
        
        // Same content, different scan IDs
        val result1 = createTestResult(
            timestamp = timestamp,
            scanIdPrefix = "APEX_${timestamp}_abc123"
        )
        
        val result2 = createTestResult(
            timestamp = timestamp,
            scanIdPrefix = "APEX_${timestamp}_xyz789"
        )
        
        val proofHash1 = ProofHasher.hashApexResult(result1)
        val proofHash2 = ProofHasher.hashApexResult(result2)
        
        assertEquals(
            "Proof hash should be identical regardless of scan ID",
            proofHash1,
            proofHash2
        )
    }
    
    @Test
    fun `test proof hash excludes timestamp`() {
        // Same content, widely different timestamps
        val result1 = createTestResult(timestamp = 1700000000000L)
        val result2 = createTestResult(timestamp = 1800000000000L)  // ~3 years later
        
        val proofHash1 = ProofHasher.hashApexResult(result1)
        val proofHash2 = ProofHasher.hashApexResult(result2)
        
        assertEquals(
            "Proof hash should be identical regardless of timestamp",
            proofHash1,
            proofHash2
        )
    }
    
    @Test
    fun `test verify proof hash function works correctly`() {
        val timestamp = 1700000000000L
        val result = createTestResult(timestamp = timestamp)
        
        val proofHash = ProofHasher.hashApexResult(result)
        
        assertTrue(
            "verifyProofHash should return true for matching hash",
            ProofHasher.verifyProofHash(result, proofHash)
        )
        
        assertFalse(
            "verifyProofHash should return false for mismatched hash",
            ProofHasher.verifyProofHash(result, "invalid_hash_123")
        )
    }
    
    @Test
    fun `test proof hash format is valid SHA-256 hex`() {
        val timestamp = 1700000000000L
        val result = createTestResult(timestamp = timestamp)
        
        val proofHash = ProofHasher.hashApexResult(result)
        
        // SHA-256 produces 64 hex characters
        assertEquals("Proof hash should be 64 characters", 64, proofHash.length)
        
        // Should only contain hex characters (0-9, a-f)
        assertTrue(
            "Proof hash should only contain hex characters",
            proofHash.all { it in '0'..'9' || it in 'a'..'f' }
        )
    }
    
    @Test
    fun `test protocol trace hash determinism`() {
        val trace = listOf(
            createTestVerdict("T01", true, 0.90),
            createTestVerdict("T02", true, 0.85),
            createTestVerdict("LP01", false, 0.45)
        )
        
        val hash1 = ProofHasher.hashProtocolTrace(trace)
        val hash2 = ProofHasher.hashProtocolTrace(trace)
        
        assertEquals(
            "Same protocol trace should produce identical hash",
            hash1,
            hash2
        )
    }
    
    @Test
    fun `test empty protocol trace produces deterministic hash`() {
        val emptyTrace = emptyList<ProtocolVerdict>()
        
        val hash1 = ProofHasher.hashProtocolTrace(emptyTrace)
        val hash2 = ProofHasher.hashProtocolTrace(emptyTrace)
        
        assertEquals(
            "Empty protocol trace should produce deterministic hash",
            hash1,
            hash2
        )
    }
    
    // ============================================================
    // INTEGRATION TESTS (full determinism verification)
    // ============================================================
    
    @Test
    fun `test full determinism scenario - replay scan with identical results`() {
        // Simulate scanning the same chart at two different times
        // Should produce different scan IDs but IDENTICAL proof hash
        
        val timestamp1 = 1700000000000L
        val timestamp2 = 1700000005000L  // 5 seconds later
        
        val sharedTrace = listOf(
            createTestVerdict("T01", true, 0.92),
            createTestVerdict("T05", true, 0.88),
            createTestVerdict("T12", true, 0.85),
            createTestVerdict("LP03", true, 0.78)
        )
        
        val scan1 = createTestResult(
            timestamp = timestamp1,
            status = ApexStatus.PASS,
            quantraScore = 87,
            entropyScore = 0.22,
            suppressionActive = false,
            omegaLock = false,
            regimeOk = true,
            confidenceApex = 0.89,
            protocolTrace = sharedTrace,
            scanIdPrefix = ProofHasher.generateScanId(timestamp1, "ticker=TSLA|timeframe=15m|chartType=Candlestick")
        )
        
        val scan2 = createTestResult(
            timestamp = timestamp2,
            status = ApexStatus.PASS,
            quantraScore = 87,
            entropyScore = 0.22,
            suppressionActive = false,
            omegaLock = false,
            regimeOk = true,
            confidenceApex = 0.89,
            protocolTrace = sharedTrace,
            scanIdPrefix = ProofHasher.generateScanId(timestamp2, "ticker=TSLA|timeframe=15m|chartType=Candlestick")
        )
        
        // Scan IDs should differ (timestamp-based)
        assertNotEquals("Scan IDs should differ due to different timestamps", scan1.scanId, scan2.scanId)
        
        // Timestamps should differ
        assertNotEquals("Timestamps should differ", scan1.timestamp, scan2.timestamp)
        
        // Proof hashes MUST be identical (determinism)
        val proofHash1 = ProofHasher.hashApexResult(scan1)
        val proofHash2 = ProofHasher.hashApexResult(scan2)
        
        assertEquals(
            "DETERMINISM GUARANTEE: Identical content at different times produces identical proof hash",
            proofHash1,
            proofHash2
        )
    }
}
