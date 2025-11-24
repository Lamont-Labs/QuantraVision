package com.lamontlabs.quantravision.quota

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.lamontlabs.quantravision.tiers.Tier
import com.lamontlabs.quantravision.tiers.TierRegistry

/**
 * SummaryQuota
 * Enforces saved summary limits based on subscription tier
 * 
 * Tier Limits:
 * - FREE: 0 saved summaries
 * - BASIC: 5 saved summaries
 * - PRO: 20 saved summaries
 * - APEX: 100 saved summaries
 */
object SummaryQuota {
    
    private const val TAG = "SummaryQuota"
    private const val PREFS_NAME = "qv_secure_prefs"
    private const val SUMMARY_COUNT_KEY = "qv_summary_count"
    
    private val lock = Any()
    
    private fun getSecurePrefs(context: Context): android.content.SharedPreferences? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize encrypted SharedPreferences", e)
            null
        }
    }
    
    private fun getCurrentTier(context: Context): Tier {
        val prefs = getSecurePrefs(context) ?: return Tier.FREE
        val tierString = prefs.getString("qv_unlocked_tier", "") ?: ""
        return Tier.fromString(tierString)
    }
    
    fun getRemainingSlots(context: Context): Int {
        synchronized(lock) {
            val prefs = getSecurePrefs(context) ?: return 0
            val tier = getCurrentTier(context)
            val limit = TierRegistry.getSavedSummaryLimit(tier)
            val used = prefs.getInt(SUMMARY_COUNT_KEY, 0)
            return maxOf(0, limit - used)
        }
    }
    
    fun canSaveSummary(context: Context): Boolean {
        return getRemainingSlots(context) > 0
    }
    
    fun recordSavedSummary(context: Context): Boolean {
        synchronized(lock) {
            if (!canSaveSummary(context)) {
                Log.w(TAG, "Summary quota exceeded")
                return false
            }
            
            val prefs = getSecurePrefs(context) ?: return false
            val currentCount = prefs.getInt(SUMMARY_COUNT_KEY, 0)
            prefs.edit()
                .putInt(SUMMARY_COUNT_KEY, currentCount + 1)
                .apply()
            
            Log.d(TAG, "Summary saved. Total: ${currentCount + 1}")
            return true
        }
    }
    
    fun recordDeletedSummary(context: Context) {
        synchronized(lock) {
            val prefs = getSecurePrefs(context) ?: return
            val currentCount = prefs.getInt(SUMMARY_COUNT_KEY, 0)
            if (currentCount > 0) {
                prefs.edit()
                    .putInt(SUMMARY_COUNT_KEY, currentCount - 1)
                    .apply()
                Log.d(TAG, "Summary deleted. Total: ${currentCount - 1}")
            }
        }
    }
    
    fun getTierLimit(context: Context): Int {
        val tier = getCurrentTier(context)
        return TierRegistry.getSavedSummaryLimit(tier)
    }
    
    fun getCurrentCount(context: Context): Int {
        synchronized(lock) {
            val prefs = getSecurePrefs(context) ?: return 0
            return prefs.getInt(SUMMARY_COUNT_KEY, 0)
        }
    }
}
