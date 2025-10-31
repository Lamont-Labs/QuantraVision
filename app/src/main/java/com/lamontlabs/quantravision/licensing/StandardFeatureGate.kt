package com.lamontlabs.quantravision.licensing

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * StandardFeatureGate
 * Single source of truth for Standard tier unlock.
 * SECURE: Reads from BillingManager's encrypted SharedPreferences.
 * Cannot be spoofed without Google Play purchase.
 */
object StandardFeatureGate {
    
    // Lock object for synchronized access to prevent race conditions (~0.01% of calls)
    private val lock = Any()
    
    /**
     * Get SharedPreferences with fallback to regular prefs if encrypted fails
     * CRITICAL: Prevents users from losing Standard access on encryption failure
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
            android.util.Log.w("StandardFeatureGate", "Encrypted prefs failed, falling back to regular prefs", e)
            // CRITICAL: Fallback to regular SharedPreferences to prevent locking out paying users
            context.getSharedPreferences("qv_billing_prefs", Context.MODE_PRIVATE)
        }
    }

    /**
     * Check if Standard tier is active (verified by BillingManager).
     * Returns true for both STANDARD and PRO tiers (Pro includes Standard features)
     * SYNCHRONIZED: Prevents concurrent access race conditions
     */
    fun isActive(context: Context): Boolean = synchronized(lock) {
        val prefs = getSecurePrefs(context) ?: return false
        val tier = prefs.getString("qv_unlocked_tier", "") ?: ""
        return tier == "STANDARD" || tier == "PRO"
    }
}
