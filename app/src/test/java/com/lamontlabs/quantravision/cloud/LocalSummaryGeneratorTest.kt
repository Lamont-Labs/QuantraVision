package com.lamontlabs.quantravision.cloud

import com.lamontlabs.quantravision.apex.models.*
import org.junit.Assert.*
import org.junit.Test

/**
 * BATCH 10: LocalSummaryGenerator Golden Tests
 * 
 * Comprehensive golden test suite for local summary generation.
 * Tests template generation for all Apex statuses with deterministic output.
 * 
 * Test Coverage:
 * 1. Golden Tests for Each Status: PASS, WAIT, FAIL, SUPPRESSED, OMEGA
 * 2. Universal Header: status, QuantraScore, confidence, entropy, regime
 * 3. Template Variables: protocol trace, invalidation points, conditions
 */
class LocalSummaryGeneratorTest {
    
    // ============================================================
    // PASS STATUS GOLDEN TESTS
    // ============================================================
    
    @Test
    fun `PASS status generates correct template`() {
        val apexResult = ApexResult(
            scanId = "test-123",
            status = ApexStatus.PASS,
            quantraScore = QuantraScoreSnapshot(0.78, 78, QuantraBand.PASS),
            protocolTrace = listOf(
                ProtocolVerdict("T45ContinuationValidation", "T45", true, 0.85, "Passed", 3.0),
                ProtocolVerdict("T50CrossRegimeCoherence", "T50", true, 0.80, "Passed", 2.5),
                ProtocolVerdict("T12VolumeConfirmation", "T12", true, 0.88, "Passed", 2.0)
            ),
            entropyScore = 0.25,
            suppressionActive = false,
            omegaLock = false,
            regimeOk = true,
            invalidationPoints = listOf("Break below support", "Volume drops significantly"),
            confidenceApex = 0.82,
            proofHash = "abc123",
            timestamp = System.currentTimeMillis()
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertTrue("Should contain PASS verdict", summary.contains("PASS"))
        assertTrue("Should contain QuantraScore", summary.contains("QuantraScore: 78/100"))
        assertTrue("Should contain confidence", summary.contains("Confidence: 82%"))
        assertTrue("Should contain entropy", summary.contains("Entropy: 25%"))
        assertTrue("Should contain regime status", summary.contains("Regime: OK"))
        
        assertTrue("Should contain structure confirmation", summary.contains("Structure confirmed"))
        assertTrue("Should contain volume/volatility alignment", summary.contains("Volume/volatility alignment"))
        
        assertTrue("Should contain top protocol T45", summary.contains("T45"))
        assertTrue("Should contain top protocol T50", summary.contains("T50"))
        
        assertTrue("Should contain first invalidation point", summary.contains("Break below support"))
        
        assertTrue("Should mention overlay", summary.contains("Overlay"))
        assertTrue("Should mention teal", summary.contains("teal"))
    }
    
    @Test
    fun `PASS status with STRONG_PASS band`() {
        val apexResult = createApexResult(
            status = ApexStatus.PASS,
            score = 92,
            band = QuantraBand.STRONG_PASS,
            confidence = 0.95
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertTrue(summary.contains("PASS"))
        assertTrue(summary.contains("92/100"))
        assertTrue(summary.contains("95%"))
    }
    
    // ============================================================
    // WAIT STATUS GOLDEN TESTS
    // ============================================================
    
    @Test
    fun `WAIT status generates correct template`() {
        val apexResult = ApexResult(
            scanId = "test-456",
            status = ApexStatus.WAIT,
            quantraScore = QuantraScoreSnapshot(0.65, 65, QuantraBand.WAIT),
            protocolTrace = listOf(
                ProtocolVerdict("T10StructureCompleteness", "T10", false, 0.55, "Incomplete", 3.0),
                ProtocolVerdict("T18RegimeValidation", "T18", true, 0.70, "Passed", 2.0)
            ),
            entropyScore = 0.45,
            suppressionActive = false,
            omegaLock = false,
            regimeOk = true,
            invalidationPoints = listOf("Pattern breaks if support violated"),
            confidenceApex = 0.62,
            proofHash = "def456",
            timestamp = System.currentTimeMillis()
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertTrue("Should contain WAIT verdict", summary.contains("WAIT"))
        assertTrue("Should contain QuantraScore", summary.contains("65/100"))
        assertTrue("Should contain confidence", summary.contains("62%"))
        assertTrue("Should contain entropy", summary.contains("45%"))
        
        assertTrue("Should mention early structure", summary.contains("Early structure"))
        assertTrue("Should mention primary blocker", summary.contains("blocker"))
        
        assertTrue("Should mention amber overlay", summary.contains("amber"))
        assertTrue("Should mention dashed overlay", summary.contains("dashed"))
        
        assertTrue("Should mention confirm condition", summary.contains("Confirm if"))
        assertTrue("Should mention break condition", summary.contains("Breaks if"))
        assertTrue("Should contain invalidation point", summary.contains("support violated"))
    }
    
    @Test
    fun `WAIT status includes confirmation conditions`() {
        val apexResult = createApexResult(
            status = ApexStatus.WAIT,
            score = 58,
            band = QuantraBand.WAIT,
            confidence = 0.60
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertTrue("Should suggest confirmation criteria", summary.contains("Confirm if"))
        assertTrue("Should suggest invalidation criteria", summary.contains("Breaks if"))
    }
    
    // ============================================================
    // FAIL STATUS GOLDEN TESTS
    // ============================================================
    
    @Test
    fun `FAIL status generates correct template`() {
        val apexResult = ApexResult(
            scanId = "test-789",
            status = ApexStatus.FAIL,
            quantraScore = QuantraScoreSnapshot(0.35, 35, QuantraBand.FAIL),
            protocolTrace = listOf(
                ProtocolVerdict("T07TrendStrengthGate", "T07", false, 0.30, "Weak trend", 3.0),
                ProtocolVerdict("T20FinalEntropyCheck", "T20", false, 0.40, "High entropy", 2.5)
            ),
            entropyScore = 0.75,
            suppressionActive = false,
            omegaLock = false,
            regimeOk = false,
            invalidationPoints = emptyList(),
            confidenceApex = 0.28,
            proofHash = "ghi789",
            timestamp = System.currentTimeMillis()
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertTrue("Should contain FAIL verdict", summary.contains("FAIL"))
        assertTrue("Should contain QuantraScore", summary.contains("35/100"))
        assertTrue("Should contain confidence", summary.contains("28%"))
        assertTrue("Should contain entropy", summary.contains("75%"))
        assertTrue("Should show regime mismatch", summary.contains("MISMATCH"))
        
        assertTrue("Should mention candidate rejected", summary.contains("rejected"))
        assertTrue("Should mention blocking gates", summary.contains("Blocking gates"))
        
        assertTrue("Should contain protocol ID", summary.contains("T07") || summary.contains("T20"))
        
        assertTrue("Should mention no overlay or fade", summary.contains("none") || summary.contains("fade"))
    }
    
    @Test
    fun `FAIL status shows rejection reason - high entropy`() {
        val apexResult = createApexResult(
            status = ApexStatus.FAIL,
            score = 42,
            band = QuantraBand.FAIL,
            entropy = 0.72,
            regimeOk = true
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertTrue("Should mention high entropy as reason", summary.contains("high entropy"))
    }
    
    @Test
    fun `FAIL status shows rejection reason - regime mismatch`() {
        val apexResult = createApexResult(
            status = ApexStatus.FAIL,
            score = 38,
            band = QuantraBand.FAIL,
            entropy = 0.30,
            regimeOk = false
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertTrue("Should mention regime mismatch as reason", summary.contains("regime mismatch"))
    }
    
    // ============================================================
    // SUPPRESSED STATUS GOLDEN TESTS
    // ============================================================
    
    @Test
    fun `SUPPRESSED status generates correct template`() {
        val apexResult = ApexResult(
            scanId = "test-sup",
            status = ApexStatus.SUPPRESSED,
            quantraScore = QuantraScoreSnapshot(0.68, 68, QuantraBand.WAIT),
            protocolTrace = listOf(
                ProtocolVerdict("T51FalsePositiveSuppression", "T51", false, 0.50, "Memory conflict", 3.0),
                ProtocolVerdict("T52PatternSuppression", "T52", false, 0.45, "Suppressed", 2.0)
            ),
            entropyScore = 0.38,
            suppressionActive = true,
            omegaLock = false,
            regimeOk = true,
            invalidationPoints = emptyList(),
            confidenceApex = 0.65,
            proofHash = "sup123",
            timestamp = System.currentTimeMillis()
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertTrue("Should contain SUPPRESSED verdict", summary.contains("SUPPRESSED"))
        assertTrue("Should mention detected but suppressed", summary.contains("suppressed"))
        assertTrue("Should mention Apex memory", summary.contains("memory"))
        
        assertTrue("Should mention suppression cause", summary.contains("cause"))
        assertTrue("Should contain protocol info", summary.contains("T51") || summary.contains("Memory conflict"))
        
        assertTrue("Should mention violet overlay", summary.contains("violet"))
        assertTrue("Should mention broken geometry", summary.contains("broken"))
    }
    
    // ============================================================
    // OMEGA STATUS GOLDEN TESTS
    // ============================================================
    
    @Test
    fun `OMEGA status generates safety lock template`() {
        val apexResult = ApexResult(
            scanId = "test-omega",
            status = ApexStatus.OMEGA,
            quantraScore = QuantraScoreSnapshot(0.0, 0, QuantraBand.FAIL),
            protocolTrace = listOf(
                ProtocolVerdict("Omega01StructuralAnomalyGuard", "Omega01", false, 0.0, "Structural anomalies detected", 5.0)
            ),
            entropyScore = 0.0,
            suppressionActive = false,
            omegaLock = true,
            regimeOk = false,
            invalidationPoints = emptyList(),
            confidenceApex = 0.0,
            proofHash = "omega123",
            timestamp = System.currentTimeMillis()
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertTrue("Should contain OMEGA verdict", summary.contains("OMEGA"))
        assertTrue("Should mention Apex Omega Safety Lock", summary.contains("Apex Omega Safety Lock"))
        assertTrue("Should mention active lock", summary.contains("active"))
        
        assertTrue("Should mention reason", summary.contains("Reason"))
        assertTrue("Should contain Omega protocol reason", summary.contains("anomal"))
        
        assertTrue("Should mention overlays disabled", summary.contains("Overlays disabled"))
        assertTrue("Should mention cloud disabled", summary.contains("cloud disabled"))
        
        assertTrue("Should mention Settings fix", summary.contains("Settings"))
        assertTrue("Should mention Health Check", summary.contains("Health Check"))
    }
    
    @Test
    fun `OMEGA status with different failure reason`() {
        val apexResult = ApexResult(
            scanId = "test-omega2",
            status = ApexStatus.OMEGA,
            quantraScore = QuantraScoreSnapshot(0.0, 0, QuantraBand.FAIL),
            protocolTrace = listOf(
                ProtocolVerdict("Omega02DataIntegrityGuard", "Omega02", false, 0.0, "Data corruption detected", 5.0)
            ),
            entropyScore = 0.0,
            suppressionActive = false,
            omegaLock = true,
            regimeOk = false,
            invalidationPoints = emptyList(),
            confidenceApex = 0.0,
            proofHash = "omega456",
            timestamp = System.currentTimeMillis()
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertTrue("Should contain Omega reason", summary.contains("corruption"))
    }
    
    // ============================================================
    // UNIVERSAL HEADER TESTS
    // ============================================================
    
    @Test
    fun `all summaries include universal header fields`() {
        val statuses = listOf(
            ApexStatus.PASS,
            ApexStatus.WAIT,
            ApexStatus.FAIL,
            ApexStatus.SUPPRESSED,
            ApexStatus.OMEGA
        )
        
        statuses.forEach { status ->
            val result = createApexResult(status = status, score = 50)
            val summary = LocalSummaryGenerator.generate(result)
            
            assertTrue("$status should contain APEX VERDICT", summary.contains("APEX VERDICT"))
            assertTrue("$status should contain QuantraScore", summary.contains("QuantraScore"))
            assertTrue("$status should contain Confidence", summary.contains("Confidence"))
            assertTrue("$status should contain Entropy", summary.contains("Entropy"))
            assertTrue("$status should contain Regime", summary.contains("Regime"))
        }
    }
    
    @Test
    fun `regime OK displays correctly`() {
        val result = createApexResult(regimeOk = true)
        val summary = LocalSummaryGenerator.generate(result)
        
        assertTrue("Should display Regime: OK", summary.contains("Regime: OK"))
    }
    
    @Test
    fun `regime MISMATCH displays correctly`() {
        val result = createApexResult(regimeOk = false)
        val summary = LocalSummaryGenerator.generate(result)
        
        assertTrue("Should display Regime: MISMATCH", summary.contains("Regime: MISMATCH"))
    }
    
    // ============================================================
    // TEMPLATE VARIABLE TESTS
    // ============================================================
    
    @Test
    fun `protocol trace top 2 included in summary`() {
        val apexResult = ApexResult(
            scanId = "test",
            status = ApexStatus.PASS,
            quantraScore = QuantraScoreSnapshot(0.80, 80, QuantraBand.PASS),
            protocolTrace = listOf(
                ProtocolVerdict("T45ContinuationValidation", "T45", true, 0.90, "Passed", 3.0),
                ProtocolVerdict("T50CrossRegimeCoherence", "T50", true, 0.85, "Passed", 2.5),
                ProtocolVerdict("T12VolumeConfirmation", "T12", true, 0.80, "Passed", 2.0),
                ProtocolVerdict("T13VolatilityAlignment", "T13", true, 0.75, "Passed", 1.5)
            ),
            entropyScore = 0.28,
            suppressionActive = false,
            omegaLock = false,
            regimeOk = true,
            invalidationPoints = emptyList(),
            confidenceApex = 0.85,
            proofHash = "test123",
            timestamp = System.currentTimeMillis()
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertTrue("Should include first protocol", summary.contains("T45"))
        assertTrue("Should include second protocol", summary.contains("T50"))
    }
    
    @Test
    fun `invalidation points included in summary`() {
        val apexResult = createApexResult(
            status = ApexStatus.PASS,
            invalidationPoints = listOf(
                "Break below support at $150",
                "Volume drops below 1M"
            )
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertTrue("Should include first invalidation point", summary.contains("Break below support"))
        assertTrue("Should include second invalidation point", summary.contains("Volume drops"))
    }
    
    @Test
    fun `empty protocol trace handled gracefully`() {
        val apexResult = createApexResult(
            status = ApexStatus.FAIL,
            protocolTrace = emptyList()
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertFalse("Should not crash with empty trace", summary.isEmpty())
        assertTrue("Should still contain status", summary.contains("FAIL"))
    }
    
    @Test
    fun `empty invalidation points handled gracefully`() {
        val apexResult = createApexResult(
            status = ApexStatus.PASS,
            invalidationPoints = emptyList()
        )
        
        val summary = LocalSummaryGenerator.generate(apexResult)
        
        assertFalse("Should not crash with empty invalidation points", summary.isEmpty())
    }
    
    // ============================================================
    // DETERMINISM TESTS
    // ============================================================
    
    @Test
    fun `identical inputs produce identical summaries`() {
        val result1 = createApexResult(status = ApexStatus.PASS, score = 85)
        val result2 = createApexResult(status = ApexStatus.PASS, score = 85)
        
        val summary1 = LocalSummaryGenerator.generate(result1)
        val summary2 = LocalSummaryGenerator.generate(result2)
        
        assertEquals("Identical inputs should produce identical summaries", summary1, summary2)
    }
    
    @Test
    fun `different scores produce different summaries`() {
        val result1 = createApexResult(status = ApexStatus.PASS, score = 85)
        val result2 = createApexResult(status = ApexStatus.PASS, score = 75)
        
        val summary1 = LocalSummaryGenerator.generate(result1)
        val summary2 = LocalSummaryGenerator.generate(result2)
        
        assertNotEquals("Different scores should produce different summaries", summary1, summary2)
    }
    
    // ============================================================
    // HELPER FUNCTIONS
    // ============================================================
    
    private fun createApexResult(
        status: ApexStatus = ApexStatus.PASS,
        score: Int = 75,
        band: QuantraBand = when {
            score >= 85 -> QuantraBand.STRONG_PASS
            score >= 70 -> QuantraBand.PASS
            score >= 50 -> QuantraBand.WAIT
            else -> QuantraBand.FAIL
        },
        confidence: Double = 0.80,
        entropy: Double = 0.30,
        regimeOk: Boolean = true,
        protocolTrace: List<ProtocolVerdict> = listOf(
            ProtocolVerdict("T45ContinuationValidation", "T45", true, 0.85, "Passed", 3.0),
            ProtocolVerdict("T50CrossRegimeCoherence", "T50", true, 0.80, "Passed", 2.5)
        ),
        invalidationPoints: List<String> = listOf("Break below support")
    ): ApexResult {
        return ApexResult(
            scanId = "test-${System.currentTimeMillis()}",
            status = status,
            quantraScore = QuantraScoreSnapshot(score / 100.0, score, band),
            protocolTrace = protocolTrace,
            entropyScore = entropy,
            suppressionActive = status == ApexStatus.SUPPRESSED,
            omegaLock = status == ApexStatus.OMEGA,
            regimeOk = regimeOk,
            invalidationPoints = invalidationPoints,
            confidenceApex = confidence,
            proofHash = "test-hash",
            timestamp = System.currentTimeMillis()
        )
    }
}
