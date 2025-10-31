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
     * Most common and essential patterns for technical analysis
     */
    private val STANDARD_TIER_PATTERNS = setOf(
        // Major Reversals (10)
        "Head_and_Shoulders",
        "Inverse_Head_and_Shoulders",
        "Double_Top",
        "Double_Bottom",
        "Triple_Top",
        "Triple_Bottom",
        "Rounding_Top",
        "Rounding_Bottom",
        "V_Top",
        "V_Bottom",
        
        // Major Continuations (10)
        "Ascending_Triangle",
        "Descending_Triangle",
        "Symmetrical_Triangle",
        "Rising_Wedge",
        "Falling_Wedge",
        "Bull_Flag",
        "Bear_Flag",
        "Bull_Pennant",
        "Bear_Pennant",
        "Cup_and_Handle",
        
        // Essential Candlesticks (10)
        "Doji",
        "Hammer",
        "Hanging_Man",
        "Inverted_Hammer",
        "Shooting_Star",
        "Bullish_Engulfing",
        "Bearish_Engulfing",
        "Morning_Star",
        "Evening_Star",
        "Piercing_Line"
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
