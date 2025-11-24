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

package com.lamontlabs.quantravision.quota

import android.content.Context
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * QuotaGate
 * Enforces daily cloud API call quota based on subscription tier.
 * Tier limits: FREE=0, PRO=10/day, ULTRA=25/day
 * Rate limits: min 8s between calls, max 3 per 60s
 * Persistent, device-local JSON: quota_state.json
 */
object QuotaGate {

    private const val FILE = "quota_state.json"
    private const val TAG = "QuotaGate"
    
    private const val TIER_FREE = "FREE"
    private const val TIER_PRO = "PRO"
    private const val TIER_ULTRA = "ULTRA"
    private const val TIER_APEX_ULTRA = "APEX_ULTRA"
    
    private const val FREE_LIMIT = 0
    private const val PRO_LIMIT = 10
    private const val ULTRA_LIMIT = 25
    
    private const val MIN_SECONDS_BETWEEN_CALLS = 8L
    private const val MAX_CALLS_PER_60_SECONDS = 3
    private const val SIXTY_SECONDS_MS = 60_000L
    
    data class QuotaState(
        val callsToday: Int,
        val lastCallTimestamp: Long,
        val lastResetDate: String,
        val tier: String,
        val recentCallTimestamps: List<Long> = emptyList()
    )

    /**
     * Check if a cloud call can be made given the current tier and quota state.
     * @param context Android context for file access
     * @param tier Current subscription tier (FREE, PRO, ULTRA, APEX_ULTRA)
     * @return true if call is allowed, false otherwise
     */
    fun canMakeCloudCall(context: Context, tier: String): Boolean {
        val normalizedTier = normalizeTier(tier)
        
        if (normalizedTier == TIER_FREE) {
            Timber.d("$TAG: FREE tier has no cloud access")
            return false
        }
        
        val state = loadState(context)
        val limit = getLimitForTier(normalizedTier)
        
        if (state.callsToday >= limit) {
            Timber.d("$TAG: Daily limit reached: ${state.callsToday}/$limit")
            return false
        }
        
        val timeSinceLast = if (state.lastCallTimestamp == 0L) {
            Long.MAX_VALUE
        } else {
            System.currentTimeMillis() - state.lastCallTimestamp
        }
        
        if (timeSinceLast < MIN_SECONDS_BETWEEN_CALLS * 1000) {
            Timber.d("$TAG: Rate limit: ${timeSinceLast}ms since last call (min ${MIN_SECONDS_BETWEEN_CALLS}s)")
            return false
        }
        
        val recentCalls = state.recentCallTimestamps
            .filter { System.currentTimeMillis() - it < SIXTY_SECONDS_MS }
        
        if (recentCalls.size >= MAX_CALLS_PER_60_SECONDS) {
            Timber.d("$TAG: Rate limit: ${recentCalls.size} calls in last 60s (max $MAX_CALLS_PER_60_SECONDS)")
            return false
        }
        
        Timber.v("$TAG: Cloud call allowed (${state.callsToday + 1}/$limit today)")
        return true
    }

    /**
     * Increment the call count and update timestamp.
     * @param context Android context for file access
     * @return true if increment succeeded, false if quota would be exceeded
     */
    fun incrementCallCount(context: Context): Boolean {
        try {
            val state = loadState(context)
            val limit = getLimitForTier(state.tier)
            
            if (state.callsToday >= limit) {
                Timber.w("$TAG: Cannot increment - limit reached")
                return false
            }
            
            val now = System.currentTimeMillis()
            val updatedRecentCalls = (state.recentCallTimestamps + now)
                .filter { now - it < SIXTY_SECONDS_MS }
                .takeLast(MAX_CALLS_PER_60_SECONDS)
            
            val newState = state.copy(
                callsToday = state.callsToday + 1,
                lastCallTimestamp = now,
                recentCallTimestamps = updatedRecentCalls
            )
            
            saveState(context, newState)
            Timber.d("$TAG: Call count incremented: ${newState.callsToday}/$limit")
            return true
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Failed to increment call count")
            return false
        }
    }

    /**
     * Get remaining calls for today.
     * @param context Android context for file access
     * @param tier Current subscription tier
     * @return Number of calls remaining
     */
    fun getRemainingCalls(context: Context, tier: String): Int {
        val normalizedTier = normalizeTier(tier)
        val state = loadState(context)
        val limit = getLimitForTier(normalizedTier)
        return (limit - state.callsToday).coerceAtLeast(0)
    }

    /**
     * Get time in milliseconds since last cloud call.
     * NOTE: This method is stateless and returns the time since the last recorded call
     * without loading state. For accurate quota checks, use canMakeCloudCall() instead.
     * @return Milliseconds since last call based on cached timestamp, or Long.MAX_VALUE if unknown
     */
    fun getTimeSinceLastCall(): Long {
        return if (lastCallTimestampCache == 0L) {
            Long.MAX_VALUE
        } else {
            System.currentTimeMillis() - lastCallTimestampCache
        }
    }
    
    private var lastCallTimestampCache: Long = 0L

    /**
     * Reset quota if needed (called on state load).
     * @param context Android context for file access
     */
    fun resetIfNeeded(context: Context) {
        loadState(context)
    }

    private fun loadState(context: Context): QuotaState {
        val file = File(context.filesDir, FILE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val nowMs = System.currentTimeMillis()
        
        if (!file.exists()) {
            val initialState = QuotaState(
                callsToday = 0,
                lastCallTimestamp = 0L,
                lastResetDate = today,
                tier = TIER_FREE,
                recentCallTimestamps = emptyList()
            )
            saveState(context, initialState)
            lastCallTimestampCache = 0L
            return initialState
        }
        
        val json = try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Corrupted quota file, recreating")
            file.delete()
            val newState = QuotaState(
                callsToday = 0,
                lastCallTimestamp = 0L,
                lastResetDate = today,
                tier = TIER_FREE,
                recentCallTimestamps = emptyList()
            )
            saveState(context, newState)
            lastCallTimestampCache = 0L
            return newState
        }
        
        val lastReset = json.optString("lastResetDate", today)
        val lastResetMs = json.optLong("lastResetMs", nowMs)
        val millisIn24Hours = 24 * 60 * 60 * 1000L
        
        val dateChanged = lastReset != today
        val dayElapsed = (nowMs - lastResetMs) >= millisIn24Hours
        
        if (dateChanged && dayElapsed) {
            Timber.d("$TAG: Daily reset triggered (date: $lastReset -> $today)")
            val resetState = QuotaState(
                callsToday = 0,
                lastCallTimestamp = json.optLong("lastCallTimestamp", 0L),
                lastResetDate = today,
                tier = json.optString("tier", TIER_FREE),
                recentCallTimestamps = emptyList()
            )
            saveState(context, resetState)
            lastCallTimestampCache = resetState.lastCallTimestamp
            return resetState
        }
        
        val recentCallsArray = json.optJSONArray("recentCallTimestamps")
        val recentCalls = mutableListOf<Long>()
        if (recentCallsArray != null) {
            for (i in 0 until recentCallsArray.length()) {
                recentCalls.add(recentCallsArray.getLong(i))
            }
        }
        
        val state = QuotaState(
            callsToday = json.optInt("callsToday", 0),
            lastCallTimestamp = json.optLong("lastCallTimestamp", 0L),
            lastResetDate = lastReset,
            tier = json.optString("tier", TIER_FREE),
            recentCallTimestamps = recentCalls
        )
        
        lastCallTimestampCache = state.lastCallTimestamp
        return state
    }

    private fun saveState(context: Context, state: QuotaState) {
        try {
            val file = File(context.filesDir, FILE)
            val json = JSONObject().apply {
                put("callsToday", state.callsToday)
                put("lastCallTimestamp", state.lastCallTimestamp)
                put("lastResetDate", state.lastResetDate)
                put("lastResetMs", System.currentTimeMillis())
                put("tier", state.tier)
                put("recentCallTimestamps", state.recentCallTimestamps)
            }
            file.writeText(json.toString(2))
            lastCallTimestampCache = state.lastCallTimestamp
            Timber.v("$TAG: State saved")
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Failed to save state")
        }
    }

    private fun normalizeTier(tier: String): String {
        return when (tier.uppercase(Locale.US)) {
            "FREE" -> TIER_FREE
            "PRO" -> TIER_PRO
            "ULTRA", "APEX_ULTRA" -> TIER_ULTRA
            else -> {
                Timber.w("$TAG: Unknown tier '$tier', defaulting to FREE")
                TIER_FREE
            }
        }
    }

    private fun getLimitForTier(tier: String): Int {
        return when (tier) {
            TIER_FREE -> FREE_LIMIT
            TIER_PRO -> PRO_LIMIT
            TIER_ULTRA -> ULTRA_LIMIT
            else -> FREE_LIMIT
        }
    }
}
