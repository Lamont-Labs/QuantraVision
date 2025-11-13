package com.lamontlabs.quantravision.licensing

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * BookFeatureGate
 * Controls access to the included trading book.
 * 
 * Pricing:
 * - Standalone: $4.99 (separate purchase)
 * - Free with Standard tier ($24.99+)
 * - Free with Pro tier ($49.99+)
 * 
 * Book Content: Your complete trading book bundled in assets/book/
 * Format: HTML/Markdown for in-app viewing
 */
object BookFeatureGate {
    
    // 🧪 TESTING BYPASS - Set to false for production
    private const val BYPASS_PAYWALLS = false
    
    /**
     * Get SharedPreferences with fallback to regular prefs if encrypted fails
     * CRITICAL: Prevents users from losing book access on encryption failure
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
            android.util.Log.w("BookFeatureGate", "Encrypted prefs failed, falling back to regular prefs", e)
            // CRITICAL: Fallback to regular SharedPreferences to prevent locking out paying users
            context.getSharedPreferences("qv_billing_prefs", Context.MODE_PRIVATE)
        }
    }

    /**
     * Check if user has access to the trading book.
     * 
     * Access granted if:
     * 1. User purchased Book ($4.99) separately, OR
     * 2. User has Standard tier ($24.99+), OR
     * 3. User has Pro tier ($49.99+)
     */
    fun hasAccess(context: Context): Boolean {
        // 🧪 TESTING: Bypass paywall
        if (BYPASS_PAYWALLS) return true
        
        val prefs = getSecurePrefs(context) ?: return false
        
        // Check if book purchased standalone
        val bookPurchased = prefs.getBoolean("qv_book_purchased", false)
        if (bookPurchased) return true
        
        // Check if Standard or Pro tier (book included free)
        val tier = prefs.getString("qv_unlocked_tier", "") ?: ""
        val normalizedTier = tier.uppercase()
        return normalizedTier == "STANDARD" || normalizedTier == "PRO"  // Normalize for backward compatibility
    }
    
    /**
     * Check if book was purchased standalone (not included with tier)
     */
    fun isStandalonePurchase(context: Context): Boolean {
        val prefs = getSecurePrefs(context) ?: return false
        return prefs.getBoolean("qv_book_purchased", false)
    }
}
