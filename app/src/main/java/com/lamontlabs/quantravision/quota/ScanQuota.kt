package com.lamontlabs.quantravision.quota

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.lamontlabs.quantravision.tiers.Tier
import com.lamontlabs.quantravision.tiers.TierRegistry
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * ScanQuota
 * Enforces daily scan limits based on subscription tier
 * 
 * Tier Limits:
 * - FREE: 3 scans/day
 * - BASIC: 25 scans/day
 * - PRO: 75 scans/day
 * - APEX: 200 scans/day
 * 
 * Resets daily at 00:00 UTC
 */
object ScanQuota {
    
    private const val TAG = "ScanQuota"
    private const val PREFS_NAME = "qv_secure_prefs"
    private const val SCAN_COUNT_KEY = "qv_scan_count"
    private const val SCAN_DATE_KEY = "qv_scan_date"
    
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
    
    private fun getTodayDateString(): String {
        return LocalDate.now(ZoneOffset.UTC).toString()
    }
    
    private fun resetIfNewDay(context: Context) {
        synchronized(lock) {
            val prefs = getSecurePrefs(context) ?: return
            val today = getTodayDateString()
            val lastScanDate = prefs.getString(SCAN_DATE_KEY, "") ?: ""
            
            if (lastScanDate != today) {
                prefs.edit()
                    .putInt(SCAN_COUNT_KEY, 0)
                    .putString(SCAN_DATE_KEY, today)
                    .apply()
                Log.d(TAG, "Reset scan quota for new day: $today")
            }
        }
    }
    
    fun getRemainingScans(context: Context): Int {
        resetIfNewDay(context)
        
        synchronized(lock) {
            val prefs = getSecurePrefs(context) ?: return 0
            val tier = getCurrentTier(context)
            val limit = TierRegistry.getScanLimit(tier)
            val used = prefs.getInt(SCAN_COUNT_KEY, 0)
            return maxOf(0, limit - used)
        }
    }
    
    fun canScan(context: Context): Boolean {
        return getRemainingScans(context) > 0
    }
    
    fun recordScan(context: Context): Boolean {
        resetIfNewDay(context)
        
        synchronized(lock) {
            if (!canScan(context)) {
                Log.w(TAG, "Scan quota exceeded")
                return false
            }
            
            val prefs = getSecurePrefs(context) ?: return false
            val currentCount = prefs.getInt(SCAN_COUNT_KEY, 0)
            prefs.edit()
                .putInt(SCAN_COUNT_KEY, currentCount + 1)
                .apply()
            
            Log.d(TAG, "Scan recorded. Total: ${currentCount + 1}")
            return true
        }
    }
    
    fun getTierLimit(context: Context): Int {
        val tier = getCurrentTier(context)
        return TierRegistry.getScanLimit(tier)
    }
}
