package com.lamontlabs.quantravision.apex

import com.lamontlabs.quantravision.apex.models.QuantraBand
import com.lamontlabs.quantravision.apex.models.QuantraScoreSnapshot
import kotlin.math.roundToInt

/**
 * BATCH 2: QuantraScore Mobile
 * 
 * QuantraScore is the Apex Engine's internal confidence metric for detected chart structures.
 * It is NOT a prediction or trading signal - it represents algorithmic confidence in pattern geometry.
 * 
 * Architecture:
 * - Raw score: 0.0-1.0 from protocol aggregation
 * - Normalized score: Clamped to 0-100 integer
 * - Band classification: FAIL/WAIT/PASS/STRONG_PASS based on thresholds
 * 
 * Band Thresholds (from master spec):
 * - FAIL: 0-49 (structure rejected or conflicts detected)
 * - WAIT: 50-69 (early structure, not confirmed)
 * - PASS: 70-84 (confirmed structure, moderate confidence)
 * - STRONG_PASS: 85-100 (strong confirmation, high confidence)
 */
object QuantraScoreMobile {
    
    /**
     * Band threshold constants (inclusive lower bound, exclusive upper bound).
     */
    object Thresholds {
        const val FAIL_MIN = 0
        const val FAIL_MAX = 49
        
        const val WAIT_MIN = 50
        const val WAIT_MAX = 69
        
        const val PASS_MIN = 70
        const val PASS_MAX = 84
        
        const val STRONG_PASS_MIN = 85
        const val STRONG_PASS_MAX = 100
    }
    
    /**
     * Normalize raw score (0.0-1.0) to integer (0-100).
     * 
     * @param rawScore Raw score from protocol aggregation (0.0-1.0)
     * @return Clamped integer score (0-100)
     */
    fun normalize(rawScore: Double): Int {
        val clamped = rawScore.coerceIn(0.0, 1.0)
        val scaled = clamped * 100.0
        return scaled.roundToInt().coerceIn(0, 100)
    }
    
    /**
     * Calculate QuantraBand from normalized score.
     * 
     * @param normalizedScore Integer score (0-100)
     * @return Corresponding QuantraBand classification
     */
    fun calculateBand(normalizedScore: Int): QuantraBand {
        return when {
            normalizedScore <= Thresholds.FAIL_MAX -> QuantraBand.FAIL
            normalizedScore <= Thresholds.WAIT_MAX -> QuantraBand.WAIT
            normalizedScore <= Thresholds.PASS_MAX -> QuantraBand.PASS
            else -> QuantraBand.STRONG_PASS
        }
    }
    
    /**
     * Create complete QuantraScore snapshot from raw score.
     * This is the primary method called by ApexEngineMobile.
     * 
     * @param rawScore Raw score from protocol aggregation (0.0-1.0)
     * @return Complete QuantraScoreSnapshot with normalized score and band
     */
    fun createSnapshot(rawScore: Double): QuantraScoreSnapshot {
        val normalized = normalize(rawScore)
        val band = calculateBand(normalized)
        
        return QuantraScoreSnapshot(
            rawScore = rawScore,
            normalizedScore = normalized,
            band = band
        )
    }
    
    /**
     * Get band display name for UI/logging.
     */
    fun getBandDisplayName(band: QuantraBand): String {
        return when (band) {
            QuantraBand.FAIL -> "FAIL"
            QuantraBand.WAIT -> "WAIT"
            QuantraBand.PASS -> "PASS"
            QuantraBand.STRONG_PASS -> "STRONG PASS"
        }
    }
    
    /**
     * Get band threshold range as string for display.
     */
    fun getBandThresholdRange(band: QuantraBand): String {
        return when (band) {
            QuantraBand.FAIL -> "${Thresholds.FAIL_MIN}-${Thresholds.FAIL_MAX}"
            QuantraBand.WAIT -> "${Thresholds.WAIT_MIN}-${Thresholds.WAIT_MAX}"
            QuantraBand.PASS -> "${Thresholds.PASS_MIN}-${Thresholds.PASS_MAX}"
            QuantraBand.STRONG_PASS -> "${Thresholds.STRONG_PASS_MIN}-${Thresholds.STRONG_PASS_MAX}"
        }
    }
}
