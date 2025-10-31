package com.lamontlabs.quantravision.detection

import com.lamontlabs.quantravision.PatternMatch
import timber.log.Timber
import kotlin.math.abs

object InvalidationDetector {

    data class InvalidationResult(
        val isInvalidated: Boolean,
        val reason: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    data class InvalidationRule(
        val patternName: String,
        val rule: String,
        val timestamp: Long
    )
    
    fun checkInvalidation(
        pattern: PatternMatch,
        previousConfidence: Double?,
        confidenceDropThreshold: Double = 0.30
    ): InvalidationResult {
        
        if (previousConfidence == null) {
            return InvalidationResult(isInvalidated = false)
        }
        
        val confidenceDrop = previousConfidence - pattern.confidence
        
        if (confidenceDrop >= confidenceDropThreshold) {
            return InvalidationResult(
                isInvalidated = true,
                reason = "Confidence dropped ${(confidenceDrop * 100).toInt()}% - pattern no longer valid"
            )
        }
        
        val patternSpecificInvalidation = checkPatternSpecificRules(pattern)
        if (patternSpecificInvalidation.isInvalidated) {
            return patternSpecificInvalidation
        }
        
        return InvalidationResult(isInvalidated = false)
    }
    
    private fun checkPatternSpecificRules(pattern: PatternMatch): InvalidationResult {
        return when {
            pattern.name.contains("Head & Shoulders", ignoreCase = true) ||
            pattern.name.contains("Head and Shoulders", ignoreCase = true) -> {
                checkHeadAndShouldersInvalidation(pattern)
            }
            pattern.name.contains("Triangle", ignoreCase = true) -> {
                checkTriangleInvalidation(pattern)
            }
            pattern.name.contains("Double Top", ignoreCase = true) ||
            pattern.name.contains("Double Bottom", ignoreCase = true) -> {
                checkDoublePatternInvalidation(pattern)
            }
            pattern.name.contains("Flag", ignoreCase = true) ||
            pattern.name.contains("Pennant", ignoreCase = true) -> {
                checkFlagInvalidation(pattern)
            }
            pattern.name.contains("Wedge", ignoreCase = true) -> {
                checkWedgeInvalidation(pattern)
            }
            else -> InvalidationResult(isInvalidated = false)
        }
    }
    
    private fun checkHeadAndShouldersInvalidation(pattern: PatternMatch): InvalidationResult {
        if (pattern.confidence < 0.40) {
            return InvalidationResult(
                isInvalidated = true,
                reason = "Neckline broken - shoulders exceed head"
            )
        }
        return InvalidationResult(isInvalidated = false)
    }
    
    private fun checkTriangleInvalidation(pattern: PatternMatch): InvalidationResult {
        if (pattern.confidence < 0.35) {
            return InvalidationResult(
                isInvalidated = true,
                reason = "Trend lines broken - no longer converging"
            )
        }
        return InvalidationResult(isInvalidated = false)
    }
    
    private fun checkDoublePatternInvalidation(pattern: PatternMatch): InvalidationResult {
        if (pattern.confidence < 0.38) {
            return InvalidationResult(
                isInvalidated = true,
                reason = "Peaks/troughs no longer aligned"
            )
        }
        return InvalidationResult(isInvalidated = false)
    }
    
    private fun checkFlagInvalidation(pattern: PatternMatch): InvalidationResult {
        if (pattern.confidence < 0.42) {
            return InvalidationResult(
                isInvalidated = true,
                reason = "Flag channel broken - pattern failed"
            )
        }
        return InvalidationResult(isInvalidated = false)
    }
    
    private fun checkWedgeInvalidation(pattern: PatternMatch): InvalidationResult {
        if (pattern.confidence < 0.36) {
            return InvalidationResult(
                isInvalidated = true,
                reason = "Wedge boundaries violated"
            )
        }
        return InvalidationResult(isInvalidated = false)
    }
    
    fun trackInvalidation(
        patternName: String,
        confidence: Double,
        previousTracking: MutableMap<String, Double>
    ): InvalidationResult {
        val previousConf = previousTracking[patternName]
        
        if (previousConf != null) {
            val drop = previousConf - confidence
            if (drop >= 0.25) {
                Timber.d("InvalidationDetector: $patternName invalidated (confidence drop: ${(drop * 100).toInt()}%)")
                previousTracking.remove(patternName)
                return InvalidationResult(
                    isInvalidated = true,
                    reason = "Pattern confidence dropped significantly"
                )
            }
        }
        
        if (confidence >= 0.50) {
            previousTracking[patternName] = confidence
        }
        
        return InvalidationResult(isInvalidated = false)
    }
    
    fun shouldAlertInvalidation(
        pattern: PatternMatch,
        invalidation: InvalidationResult
    ): Boolean {
        if (!invalidation.isInvalidated) return false
        
        return pattern.confidence < 0.40 && invalidation.reason != null
    }
}
