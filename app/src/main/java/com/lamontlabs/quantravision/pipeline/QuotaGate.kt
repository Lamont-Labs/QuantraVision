/*
 * Copyright (c) 2025 Lamont Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lamontlabs.quantravision.pipeline

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.tiers.Tier
import com.lamontlabs.quantravision.tiers.TierRegistry
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * QuotaGate
 * 
 * Master Spec v2.0 Quota Enforcement Layer
 * 
 * Hard-capped daily limits per tier:
 * - FREE:    3 scans/day,   1 narration/day
 * - STARTER: 25 scans/day,  5 narrations/day
 * - PRO:     75 scans/day,  20 narrations/day
 * - APEX:    200 scans/day, 60 narrations/day
 * 
 * Quota resets at 00:00 UTC daily.
 * Uses encrypted SharedPreferences for tamper resistance.
 * 
 * The live pipeline stops before image processing when scan storage is
 * unavailable or the daily scan limit is exhausted.
 */
class QuotaGate private constructor(
    private val context: Context,
    private val storage: (Context) -> SharedPreferences?
) : ScanQuota {
    constructor(context: Context) : this(context, { openEncryptedStorage(it) })
    internal constructor(context: Context, prefs: SharedPreferences) : this(context, { prefs })

    companion object {
        private const val TAG = "QuotaGate"
        private val LOCK = Any()
        private const val PREFS_NAME = "qv_quota_gate"
        private const val KEY_SCAN_COUNT = "scan_count"
        private const val KEY_NARRATION_COUNT = "narration_count"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private fun openEncryptedStorage(context: Context): SharedPreferences? = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context, PREFS_NAME, masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (error: Exception) {
            Timber.e(error, "$TAG: Quota storage unavailable")
            null
        }
    }

    private val lock get() = LOCK

    data class QuotaCheckResult(
        val allowed: Boolean,
        val remaining: Int,
        val limit: Int,
        val tier: Tier
    )

    private fun getSecurePrefs(): SharedPreferences? = storage(context)

    private fun resetIfNewDay() {
        synchronized(lock) {
            val prefs = getSecurePrefs() ?: return
            val todayUtc = LocalDate.now(ZoneOffset.UTC).toString()
            val lastResetDate = prefs.getString(KEY_LAST_RESET_DATE, "") ?: ""

            if (lastResetDate != todayUtc) {
                Timber.i("$TAG: New day detected, resetting quotas")
                prefs.edit()
                    .putInt(KEY_SCAN_COUNT, 0)
                    .putInt(KEY_NARRATION_COUNT, 0)
                    .putString(KEY_LAST_RESET_DATE, todayUtc)
                    .apply()
            }
        }
    }

    /**
     * Check if user can make a scan based on current quota.
     */
    override fun checkScanQuota(): QuotaCheckResult {
        resetIfNewDay()
        
        synchronized(lock) {
            val tier = EntitlementManager.getCurrentTier(context)
            val limit = TierRegistry.getScanLimit(tier)
            val prefs = getSecurePrefs()
            val currentCount = prefs?.getInt(KEY_SCAN_COUNT, 0) ?: 0
            val remaining = (limit - currentCount).coerceAtLeast(0)
            val allowed = prefs != null && remaining > 0

            Timber.d("$TAG: Scan quota check - tier=$tier, used=$currentCount, limit=$limit, remaining=$remaining")

            return QuotaCheckResult(
                allowed = allowed,
                remaining = remaining,
                limit = limit,
                tier = tier
            )
        }
    }

    /**
     * Check if user can request cloud narration based on current quota.
     */
    fun checkNarrationQuota(): QuotaCheckResult {
        resetIfNewDay()
        
        synchronized(lock) {
            val tier = EntitlementManager.getCurrentTier(context)
            val limit = TierRegistry.getAIExplanationLimit(tier)
            val prefs = getSecurePrefs()
            val currentCount = prefs?.getInt(KEY_NARRATION_COUNT, 0) ?: 0
            val remaining = (limit - currentCount).coerceAtLeast(0)
            val allowed = prefs != null && remaining > 0

            Timber.d("$TAG: Narration quota check - tier=$tier, used=$currentCount, limit=$limit, remaining=$remaining")

            return QuotaCheckResult(
                allowed = allowed,
                remaining = remaining,
                limit = limit,
                tier = tier
            )
        }
    }

    /**
     * Record a scan usage. Call after successful scan completion.
     */
    override fun recordScan(): Boolean {
        resetIfNewDay()
        
        synchronized(lock) {
            val prefs = getSecurePrefs() ?: return false
            val currentCount = prefs.getInt(KEY_SCAN_COUNT, 0)
            val limit = TierRegistry.getScanLimit(EntitlementManager.getCurrentTier(context))
            if (currentCount >= limit) return false
            return prefs.edit().putInt(KEY_SCAN_COUNT, currentCount + 1).commit()
        }
    }

    /**
     * Record a narration usage. Call after successful cloud narration.
     */
    fun recordNarration(): Boolean {
        resetIfNewDay()
        
        synchronized(lock) {
            val prefs = getSecurePrefs() ?: return false
            val currentCount = prefs.getInt(KEY_NARRATION_COUNT, 0)
            prefs.edit()
                .putInt(KEY_NARRATION_COUNT, currentCount + 1)
                .apply()
            
            Timber.d("$TAG: Narration recorded. Total: ${currentCount + 1}")
            return true
        }
    }

    /**
     * Get remaining scans for current tier.
     */
    fun getRemainingScan(): Int {
        return checkScanQuota().remaining
    }

    /**
     * Get remaining narrations for current tier.
     */
    fun getRemainingNarrations(): Int {
        return checkNarrationQuota().remaining
    }

    /**
     * Get quota summary for UI display.
     */
    fun getQuotaSummary(): QuotaSummary {
        val scanResult = checkScanQuota()
        val narrationResult = checkNarrationQuota()
        
        return QuotaSummary(
            tier = scanResult.tier,
            scansUsed = scanResult.limit - scanResult.remaining,
            scansLimit = scanResult.limit,
            narrationsUsed = narrationResult.limit - narrationResult.remaining,
            narrationsLimit = narrationResult.limit
        )
    }

    data class QuotaSummary(
        val tier: Tier,
        val scansUsed: Int,
        val scansLimit: Int,
        val narrationsUsed: Int,
        val narrationsLimit: Int
    ) {
        val scansRemaining: Int get() = (scansLimit - scansUsed).coerceAtLeast(0)
        val narrationsRemaining: Int get() = (narrationsLimit - narrationsUsed).coerceAtLeast(0)
        val scansExhausted: Boolean get() = scansRemaining <= 0
        val narrationsExhausted: Boolean get() = narrationsRemaining <= 0
    }
}
