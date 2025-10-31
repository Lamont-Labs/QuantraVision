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
 * Tier Structure (Option 1 Pricing):
 * - Free: 3 highlights/day, 10 BASIC patterns only
 * - Standard ($14.99): Unlimited highlights, 30 core patterns + Regime Navigator
 * - Pro ($29.99): Unlimited highlights, all 108 patterns + 4 intelligence features
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
            else -> Tier.FREE
        }
    }

    /**
     * Check if a specific pattern is available for the current tier
     */
    fun isPatternAvailable(context: Context, patternId: String): Boolean {
        return when (getCurrentTier(context)) {
            Tier.PRO -> true // All 108 patterns
            Tier.STANDARD -> STANDARD_TIER_PATTERNS.contains(patternId)
            Tier.FREE -> FREE_TIER_PATTERNS.contains(patternId)
        }
    }

    /**
     * Filter pattern matches to only include patterns available for current tier
     */
    fun filterByTier(context: Context, matches: List<PatternMatch>): List<PatternMatch> {
        return when (getCurrentTier(context)) {
            Tier.PRO -> matches // All 108 patterns available
            Tier.STANDARD -> matches.filter { STANDARD_TIER_PATTERNS.contains(it.patternId) }
            Tier.FREE -> matches.filter { FREE_TIER_PATTERNS.contains(it.patternId) }
        }
    }

    /**
     * Get count of available patterns for current tier
     */
    fun getAvailablePatternCount(context: Context): Int {
        return when (getCurrentTier(context)) {
            Tier.PRO -> 108
            Tier.STANDARD -> STANDARD_TIER_PATTERNS.size // 30
            Tier.FREE -> FREE_TIER_PATTERNS.size // 10
        }
    }

    /**
     * Get count of locked patterns for current tier
     */
    fun getLockedPatternCount(context: Context): Int {
        return when (getCurrentTier(context)) {
            Tier.PRO -> 0
            Tier.STANDARD -> 108 - STANDARD_TIER_PATTERNS.size // 78
            Tier.FREE -> 108 - FREE_TIER_PATTERNS.size // 98
        }
    }

    enum class Tier {
        FREE,      // 3 highlights/day, 10 basic patterns
        STANDARD,  // Unlimited highlights, 30 core patterns, Regime Navigator
        PRO        // Unlimited highlights, all 108 patterns, 4 intelligence features
    }
}
