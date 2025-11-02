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
 * - Free: $0 - 10 patterns, basic overlay
 * - Starter ($9.99): 25 patterns, multi-timeframe, basic analytics
 * - Standard ($24.99): 50 patterns, full analytics, 50 achievements, 25 lessons, book, exports
 * - Pro ($49.99): 102 patterns, Intelligence Stack, AI Learning, Behavioral Guardrails, Proof Capsules
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
     * Starter Tier Patterns (25 patterns total = Free 10 + 15 more)
     * IDs match actual YAML filenames (lowercase snake_case)
     */
    val STARTER_TIER_PATTERNS = FREE_TIER_PATTERNS + setOf(
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
     * Standard Tier Patterns (50 patterns total = Starter 25 + 25 more)
     * IDs match actual YAML filenames (lowercase snake_case)
     */
    val STANDARD_TIER_PATTERNS = STARTER_TIER_PATTERNS + setOf(
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
            ProFeatureGate.isActive(context) -> Tier.PRO
            StandardFeatureGate.isActive(context) -> Tier.STANDARD
            StarterFeatureGate.isActive(context) -> Tier.STARTER
            else -> Tier.FREE
        }
    }

    /**
     * Check if a specific pattern is available for the current tier
     */
    fun isPatternAvailable(context: Context, patternId: String): Boolean {
        return when (getCurrentTier(context)) {
            Tier.PRO -> true // All 102 patterns
            Tier.STANDARD -> STANDARD_TIER_PATTERNS.contains(patternId)
            Tier.STARTER -> STARTER_TIER_PATTERNS.contains(patternId)
            Tier.FREE -> FREE_TIER_PATTERNS.contains(patternId)
        }
    }

    /**
     * Filter pattern matches to only include patterns available for current tier
     */
    fun filterByTier(context: Context, matches: List<PatternMatch>): List<PatternMatch> {
        return when (getCurrentTier(context)) {
            Tier.PRO -> matches // All 102 patterns available
            Tier.STANDARD -> matches.filter { STANDARD_TIER_PATTERNS.contains(it.patternId) }
            Tier.STARTER -> matches.filter { STARTER_TIER_PATTERNS.contains(it.patternId) }
            Tier.FREE -> matches.filter { FREE_TIER_PATTERNS.contains(it.patternId) }
        }
    }

    /**
     * Get count of available patterns for current tier
     */
    fun getAvailablePatternCount(context: Context): Int {
        return when (getCurrentTier(context)) {
            Tier.PRO -> 102
            Tier.STANDARD -> STANDARD_TIER_PATTERNS.size // 50
            Tier.STARTER -> STARTER_TIER_PATTERNS.size // 25
            Tier.FREE -> FREE_TIER_PATTERNS.size // 10
        }
    }

    /**
     * Get count of locked patterns for current tier
     */
    fun getLockedPatternCount(context: Context): Int {
        return when (getCurrentTier(context)) {
            Tier.PRO -> 0
            Tier.STANDARD -> 102 - STANDARD_TIER_PATTERNS.size // 52
            Tier.STARTER -> 102 - STARTER_TIER_PATTERNS.size // 77
            Tier.FREE -> 102 - FREE_TIER_PATTERNS.size // 92
        }
    }

    enum class Tier {
        FREE,      // $0 - 10 patterns, basic overlay
        STARTER,   // $9.99 - 25 patterns, multi-timeframe, basic analytics
        STANDARD,  // $24.99 - 50 patterns, full analytics, achievements, lessons, book, exports
        PRO        // $49.99 - 102 patterns, Intelligence Stack, AI Learning, Behavioral Guardrails, Proof Capsules
    }
}
