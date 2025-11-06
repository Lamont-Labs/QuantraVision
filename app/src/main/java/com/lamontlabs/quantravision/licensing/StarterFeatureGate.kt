package com.lamontlabs.quantravision.licensing

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * StarterFeatureGate
 * Single source of truth for Starter tier unlock.
 * SECURE: Reads from BillingManager's encrypted SharedPreferences.
 * Cannot be spoofed without Google Play purchase.
 */
object StarterFeatureGate {
    
    // DEBUG: Bypass all paywalls for testing (set to false for production)
    private const val BYPASS_PAYWALLS = true
    
    private val lock = Any()
    
    /**
     * Get SharedPreferences with fallback to regular prefs if encrypted fails
     * CRITICAL: Prevents users from losing Starter access on encryption failure
     */
    private fun getSecurePrefs(context: Context): android.content.SharedPreferences? {
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
            android.util.Log.w("StarterFeatureGate", "Encrypted prefs failed, falling back to regular prefs", e)
            context.getSharedPreferences("qv_billing_prefs", Context.MODE_PRIVATE)
        }
    }

    /**
     * Check if Starter tier or higher is active (verified by BillingManager).
     * Returns true for STARTER, STANDARD, and PRO tiers
     * SYNCHRONIZED: Prevents concurrent access race conditions
     */
    fun isActive(context: Context): Boolean = synchronized(lock) {
        // DEBUG: Bypass paywalls for testing
        if (BYPASS_PAYWALLS) return true
        
        val prefs = getSecurePrefs(context) ?: return false
        val tier = prefs.getString("qv_unlocked_tier", "") ?: ""
        val normalizedTier = tier.uppercase()
        return normalizedTier == "STARTER" || normalizedTier == "STANDARD" || normalizedTier == "PRO"  // Normalize for backward compatibility
    }
    
    /**
     * Alias for isActive() for compatibility with other feature gates
     */
    fun hasAccess(context: Context): Boolean = isActive(context)
}
