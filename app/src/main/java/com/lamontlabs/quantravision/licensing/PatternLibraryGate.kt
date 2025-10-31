package com.lamontlabs.quantravision.licensing

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch

/**
 * PatternLibraryGate
 * Controls which patterns are available based on license tier.
 * 
 * Tier Structure:
 * - Free: 3-5 highlights of BASIC patterns (30 core patterns)
 * - Standard: Unlimited highlights of BASIC patterns (30 core patterns)
 * - Pro: Unlimited highlights of ALL patterns (108 patterns)
 */
object PatternLibraryGate {

    /**
     * Standard Tier Core Patterns (30 patterns)
     * IDs match actual YAML filenames (lowercase snake_case)
     */
    val STANDARD_TIER_PATTERNS = setOf(
        // Major Reversals (10)
        "head_and_shoulders",
        "inverse_head_and_shoulders",
        "double_top",
        "double_bottom",
        "triple_top",
        "triple_bottom",
        "rounding_top",
        "rounding_bottom",
        "v_top",
        "v_bottom",
        
        // Major Continuations (10)
        "ascending_triangle",
        "descending_triangle",
        "symmetrical_triangle",
        "rising_wedge",
        "falling_wedge",
        "bull_flag",
        "bear_flag",
        "bull_pennant",
        "bear_pennant",
        "cup_and_handle",
        
        // Essential Candlesticks (10)
        "doji",
        "hammer",
        "hanging_man",
        "inverted_hammer",
        "shooting_star",
        "bullish_engulfing",
        "bearish_engulfing",
        "morning_star",
        "evening_star",
        "three_white_soldiers"
    )

    /**
     * Get current license tier
     */
    fun getCurrentTier(context: Context): Tier {
        return when {
            ProFeatureGate.isActive(context) -> Tier.PRO
            StandardFeatureGate.isActive(context) -> Tier.STANDARD
            else -> Tier.FREE
        }
    }

    /**
     * Check if a specific pattern is available for the current tier
     */
    fun isPatternAvailable(context: Context, patternId: String): Boolean {
        return when (getCurrentTier(context)) {
            Tier.PRO -> true // All 108 patterns
            Tier.STANDARD, Tier.FREE -> STANDARD_TIER_PATTERNS.contains(patternId)
        }
    }

    /**
     * Filter pattern matches to only include patterns available for current tier
     */
    fun filterByTier(context: Context, matches: List<PatternMatch>): List<PatternMatch> {
        if (getCurrentTier(context) == Tier.PRO) {
            return matches // All patterns available
        }
        
        // Filter to Standard tier patterns only
        return matches.filter { match ->
            STANDARD_TIER_PATTERNS.contains(match.patternId)
        }
    }

    /**
     * Get count of available patterns for current tier
     */
    fun getAvailablePatternCount(context: Context): Int {
        return when (getCurrentTier(context)) {
            Tier.PRO -> 108
            Tier.STANDARD, Tier.FREE -> STANDARD_TIER_PATTERNS.size // 30
        }
    }

    /**
     * Get count of locked patterns for current tier
     */
    fun getLockedPatternCount(context: Context): Int {
        return when (getCurrentTier(context)) {
            Tier.PRO -> 0
            Tier.STANDARD, Tier.FREE -> 108 - STANDARD_TIER_PATTERNS.size // 78
        }
    }

    enum class Tier {
        FREE,      // 3-5 highlights, 30 basic patterns
        STANDARD,  // Unlimited highlights, 30 basic patterns
        PRO        // Unlimited highlights, all 108 patterns
    }
}
