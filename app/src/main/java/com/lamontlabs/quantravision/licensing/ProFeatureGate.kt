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
    
    private fun getSecurePrefs(context: Context) = runCatching {
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
    }.getOrNull()

    /**
     * Check if Pro tier is active (verified by BillingManager)
     */
    fun isActive(context: Context): Boolean {
        val prefs = getSecurePrefs(context) ?: return false
        val tier = prefs.getString("qv_unlocked_tier", "") ?: ""
        return tier == "PRO"
    }
}
