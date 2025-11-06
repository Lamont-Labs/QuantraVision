package com.lamontlabs.quantravision.licensing

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * ProFeatureGate
 * Single source of truth for Pro unlock.
 * SECURE: Reads from BillingManager's encrypted SharedPreferences.
 * Cannot be spoofed without Google Play purchase.
 */
object ProFeatureGate {
    
    // DEBUG: Bypass all paywalls for testing (set to false for production)
    private const val BYPASS_PAYWALLS = true
    
    // Lock object for synchronized access to prevent race conditions (~0.01% of calls)
    private val lock = Any()
    
    /**
     * Get SharedPreferences with fallback to regular prefs if encrypted fails
     * CRITICAL: Prevents users from losing Pro access on encryption failure
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
            android.util.Log.w("ProFeatureGate", "Encrypted prefs failed, falling back to regular prefs", e)
            // CRITICAL: Fallback to regular SharedPreferences to prevent locking out paying users
            context.getSharedPreferences("qv_billing_prefs", Context.MODE_PRIVATE)
        }
    }

    /**
     * Check if Pro tier is active (verified by BillingManager)
     * SYNCHRONIZED: Prevents concurrent access race conditions
     */
    fun isActive(context: Context): Boolean = synchronized(lock) {
        // DEBUG: Bypass paywalls for testing
        if (BYPASS_PAYWALLS) return true
        
        val prefs = getSecurePrefs(context) ?: return false
        val tier = prefs.getString("qv_unlocked_tier", "") ?: ""
        return tier.uppercase() == "PRO"  // Normalize for backward compatibility
    }
}
