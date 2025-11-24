package com.lamontlabs.quantravision.licensing

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.lamontlabs.quantravision.PatternMatch

/**
 * PatternLibraryGate
 * Controls which patterns are available based on license tier.
 * 
 * Tier Structure (4 Tiers):
 * - Free: $0/mo - 10 patterns, basic overlay
 * - Basic ($4.99/mo): 25 patterns, core overlay
 * - Pro ($14.99/mo): 50 patterns, full apex overlay, batch mode
 * - Apex ($29.99/mo): 109 patterns, advanced logic, AI learning
 */
object PatternLibraryGate {

    /**
     * Free Tier Patterns (10 most essential patterns)
     * IDs match actual YAML filenames (lowercase snake_case)
     */
    val FREE_TIER_PATTERNS = setOf(
        // Major Reversals (4)
        "head_and_shoulders",
        "inverse_head_and_shoulders",
        "double_top",
        "double_bottom",
        
        // Major Continuations (3)
        "ascending_triangle",
        "descending_triangle",
        "symmetrical_triangle",
        
        // Essential Candlesticks (3)
        "doji",
        "hammer",
        "bullish_engulfing"
    )

    /**
     * Basic Tier Patterns (25 patterns total = Free 10 + 15 more)
     * IDs match actual YAML filenames (lowercase snake_case)
     */
    val BASIC_TIER_PATTERNS = FREE_TIER_PATTERNS + setOf(
        // Additional Reversals (6)
        "triple_top",
        "triple_bottom",
        "rounding_top",
        "rounding_bottom",
        "v_top",
        "v_bottom",
        
        // Additional Continuations (5)
        "rising_wedge",
        "falling_wedge",
        "bull_flag",
        "bear_flag",
        "cup_and_handle",
        
        // Additional Candlesticks (4)
        "bearish_engulfing",
        "morning_star",
        "evening_star",
        "inverted_hammer"
    )

    /**
     * Pro Tier Patterns (50 patterns total = Basic 25 + 25 more)
     * IDs match actual YAML filenames (lowercase snake_case)
     */
    val PRO_TIER_PATTERNS = BASIC_TIER_PATTERNS + setOf(
        // Advanced Candlesticks (13)
        "hanging_man",
        "shooting_star",
        "spinning_top",
        "marubozu_bullish",
        "marubozu_bearish",
        "three_white_soldiers",
        "three_black_crows",
        "harami_bullish",
        "harami_bearish",
        "dark_cloud_cover",
        "piercing_line",
        "tweezer_top",
        "tweezer_bottom",
        
        // Complex Patterns (12)
        "bull_pennant",
        "bear_pennant",
        "diamond_top",
        "diamond_bottom",
        "island_reversal_top",
        "island_reversal_bottom",
        "broadening_top",
        "broadening_bottom",
        "rectangle_bullish",
        "rectangle_bearish",
        "ascending_channel",
        "descending_channel"
    )

    /**
     * Get SharedPreferences with fallback to regular prefs if encrypted fails
     * CRITICAL: Prevents users from losing purchased patterns on encryption failure
     */
    private fun getPrefsWithFallback(context: Context): android.content.SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "qv_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.w("PatternLibraryGate", "Encrypted prefs failed, falling back to regular prefs", e)
            // CRITICAL: Fallback to regular SharedPreferences to prevent locking out paying users
            context.getSharedPreferences("qv_billing_prefs", Context.MODE_PRIVATE)
        }
    }
    
    /**
     * Get current license tier
     */
    fun getCurrentTier(context: Context): Tier {
        return when {
            ApexFeatureGate.isActive(context) -> Tier.APEX
            ProFeatureGate.isActive(context) -> Tier.PRO
            BasicFeatureGate.isActive(context) -> Tier.BASIC
            else -> Tier.FREE
        }
    }

    /**
     * Check if a specific pattern is available for the current tier
     */
    fun isPatternAvailable(context: Context, patternId: String): Boolean {
        return when (getCurrentTier(context)) {
            Tier.APEX -> true // All 109 patterns
            Tier.PRO -> PRO_TIER_PATTERNS.contains(patternId)
            Tier.BASIC -> BASIC_TIER_PATTERNS.contains(patternId)
            Tier.FREE -> FREE_TIER_PATTERNS.contains(patternId)
        }
    }

    /**
     * Filter pattern matches to only include patterns available for current tier
     */
    fun filterByTier(context: Context, matches: List<PatternMatch>): List<PatternMatch> {
        return when (getCurrentTier(context)) {
            Tier.APEX -> matches // All 109 patterns available
            Tier.PRO -> matches.filter { PRO_TIER_PATTERNS.contains(it.patternName) }
            Tier.BASIC -> matches.filter { BASIC_TIER_PATTERNS.contains(it.patternName) }
            Tier.FREE -> matches.filter { FREE_TIER_PATTERNS.contains(it.patternName) }
        }
    }

    /**
     * Get count of available patterns for current tier
     */
    fun getAvailablePatternCount(context: Context): Int {
        return when (getCurrentTier(context)) {
            Tier.APEX -> 109
            Tier.PRO -> PRO_TIER_PATTERNS.size // 50
            Tier.BASIC -> BASIC_TIER_PATTERNS.size // 25
            Tier.FREE -> FREE_TIER_PATTERNS.size // 10
        }
    }

    /**
     * Get count of locked patterns for current tier
     */
    fun getLockedPatternCount(context: Context): Int {
        return when (getCurrentTier(context)) {
            Tier.APEX -> 0
            Tier.PRO -> 109 - PRO_TIER_PATTERNS.size // 59
            Tier.BASIC -> 109 - BASIC_TIER_PATTERNS.size // 84
            Tier.FREE -> 109 - FREE_TIER_PATTERNS.size // 99
        }
    }

    enum class Tier {
        FREE,   // $0/mo - 10 patterns, basic overlay
        BASIC,  // $4.99/mo - 25 patterns, core overlay
        PRO,    // $14.99/mo - 50 patterns, full apex overlay, batch mode
        APEX    // $29.99/mo - 109 patterns, advanced logic, AI learning
    }
}
