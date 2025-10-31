package com.lamontlabs.quantravision.licensing

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * BookFeatureGate
 * Controls access to the included trading book.
 * 
 * Option 1 Pricing:
 * - Standalone: $2.99 (separate purchase)
 * - Free with Standard tier ($14.99+)
 * - Free with Pro tier ($29.99+)
 * 
 * Book Content: Your complete trading book bundled in assets/book/
 * Format: HTML/Markdown for in-app viewing
 */
object BookFeatureGate {
    
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
     * Check if user has access to the trading book.
     * 
     * Access granted if:
     * 1. User purchased Book ($2.99) separately, OR
     * 2. User has Standard tier ($14.99+), OR
     * 3. User has Pro tier ($29.99+)
     */
    fun hasAccess(context: Context): Boolean {
        val prefs = getSecurePrefs(context) ?: return false
        
        // Check if book purchased standalone
        val bookPurchased = prefs.getBoolean("qv_book_purchased", false)
        if (bookPurchased) return true
        
        // Check if Standard or Pro tier (book included free)
        val tier = prefs.getString("qv_unlocked_tier", "") ?: ""
        return tier == "STANDARD" || tier == "PRO"
    }
    
    /**
     * Check if book was purchased standalone (not included with tier)
     */
    fun isStandalonePurchase(context: Context): Boolean {
        val prefs = getSecurePrefs(context) ?: return false
        return prefs.getBoolean("qv_book_purchased", false)
    }
}
