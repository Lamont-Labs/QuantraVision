package com.lamontlabs.quantravision.apex.models

/**
 * BATCH 2: Apex Core Models
 * 
 * Shared data models for the Apex Engine Mobile pipeline.
 * These models form the contract between vision processing, Apex Engine,
 * overlays, and cloud narration.
 */

/**
 * Input context for Apex Engine scan.
 * Contains metadata about the chart being analyzed.
 * 
 * @property ticker Symbol or asset identifier (from OCR or manual)
 * @property timeframe Detected or user-specified timeframe
 * @property chartType Type of chart (candlestick, line, bar, etc.)
 * @property timestamp Scan timestamp in milliseconds
 * @property userId Anonymous user identifier for quota tracking
 * @property tier User subscription tier (FREE, PRO, APEX_ULTRA)
 */
data class ApexScanContext(
    val ticker: String?,
    val timeframe: String?,
    val chartType: String,
    val timestamp: Long,
    val userId: String,
    val tier: SubscriptionTier
)

/**
 * Subscription tier enumeration.
 */
enum class SubscriptionTier {
    FREE,
    PRO,
    APEX_ULTRA
}

/**
 * Vision model outputs - primitive chart elements extracted locally.
 * 
 * TODO BATCH 3+: Add fields for:
 * - Detected candlesticks/bars/lines
 * - Trendlines and channels
 * - Support/resistance levels
 * - Volume profile
 * - Indicator readings
 * - Price action geometry
 * 
 * @property rawImageHash Perceptual hash of input image for deduplication
 * @property detectedObjects Placeholder for YOLO detection results
 * @property extractedLines Placeholder for Canny/Hough line results
 * @property ocrResults Placeholder for ticker/timeframe OCR results
 */
data class ChartPrimitives(
    val rawImageHash: String,
    val detectedObjects: List<String> = emptyList(),
    val extractedLines: List<String> = emptyList(),
    val ocrResults: Map<String, String> = emptyMap()
) {
    companion object {
        fun stub(): ChartPrimitives = ChartPrimitives(
            rawImageHash = "STUB_HASH_BATCH2",
            detectedObjects = emptyList(),
            extractedLines = emptyList(),
            ocrResults = emptyMap()
        )
    }
}

/**
 * Complete Apex Engine scan result.
 * This is the authoritative output of the deterministic pipeline.
 * 
 * @property scanId Unique identifier for this scan
 * @property status Final Apex verdict (PASS, WAIT, FAIL, SUPPRESSED, OMEGA)
 * @property quantraScore QuantraScore snapshot with band classification
 * @property protocolTrace Ordered trace of protocol verdicts
 * @property entropyScore Entropy/uncertainty score (0.0-1.0)
 * @property suppressionActive Whether suppression override is active
 * @property omegaLock Whether Omega safety lock is engaged
 * @property regimeOk Whether market regime matches pattern expectations
 * @property invalidationPoints Key price levels that would invalidate the pattern
 * @property confidenceApex Overall Apex confidence (0.0-1.0)
 * @property proofHash SHA-256 hash of canonicalized result for audit trail
 * @property timestamp Scan completion timestamp
 */
data class ApexResult(
    val scanId: String,
    val status: ApexStatus,
    val quantraScore: QuantraScoreSnapshot,
    val protocolTrace: List<ProtocolVerdict>,
    val entropyScore: Double,
    val suppressionActive: Boolean,
    val omegaLock: Boolean,
    val regimeOk: Boolean,
    val invalidationPoints: List<String>,
    val confidenceApex: Double,
    val proofHash: String,
    val timestamp: Long
)

/**
 * Apex Engine status enumeration.
 * Maps to overlay rendering and cloud narration logic.
 */
enum class ApexStatus {
    PASS,           // Structure confirmed, solid teal overlays
    WAIT,           // Early structure, amber dashed overlays
    FAIL,           // Rejected, no overlays
    SUPPRESSED,     // Detected but suppressed, violet broken overlays
    OMEGA           // Safety lock active, overlays disabled
}

/**
 * Individual protocol evaluation verdict.
 * Used for trace logging and debugging.
 * 
 * @property protocolId Protocol identifier (e.g., "T01", "LP03", "Omega01")
 * @property protocolName Human-readable protocol name
 * @property passed Whether this protocol passed
 * @property confidence Protocol-specific confidence (0.0-1.0)
 * @property reason Brief explanation of verdict
 * @property weight Protocol weight in final score calculation
 */
data class ProtocolVerdict(
    val protocolId: String,
    val protocolName: String,
    val passed: Boolean,
    val confidence: Double,
    val reason: String,
    val weight: Double
)

/**
 * QuantraScore snapshot with band classification.
 * See QuantraScoreMobile.kt for normalization and band logic.
 * 
 * @property rawScore Raw score before normalization (0.0-1.0)
 * @property normalizedScore Clamped integer score (0-100)
 * @property band Classification band (FAIL, WAIT, PASS, STRONG_PASS)
 */
data class QuantraScoreSnapshot(
    val rawScore: Double,
    val normalizedScore: Int,
    val band: QuantraBand
)

/**
 * QuantraBand enumeration.
 * See QuantraScoreMobile.kt for threshold definitions.
 */
enum class QuantraBand {
    FAIL,           // 0-49
    WAIT,           // 50-69
    PASS,           // 70-84
    STRONG_PASS     // 85-100
}
