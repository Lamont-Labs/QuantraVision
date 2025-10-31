package com.lamontlabs.quantravision.quota

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

/**
 * HighlightQuota
 * Enforces daily pattern highlight quota for Free tier.
 * Option 1 Pricing: 3 highlights per day, resets daily at midnight.
 * Persistent, device-local, deterministic JSON: highlight_quota.json
 */
object HighlightQuota {

    private const val FILE = "highlight_quota.json"
    private const val DAILY_LIMIT = 3 // Free tier gets 3 highlights per day

    data class State(
        val count: Int,
        val limit: Int,
        val lastResetDate: String,
        val firstUseDate: String
    )

    fun state(context: Context): State {
        val f = File(context.filesDir, FILE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        
        if (!f.exists()) {
            val o = JSONObject().apply {
                put("count", 0)
                put("limit", DAILY_LIMIT)
                put("lastResetDate", today)
                put("firstUse", today)
            }
            f.writeText(o.toString(2))
            return State(0, DAILY_LIMIT, today, today)
        }
        
        val o = JSONObject(f.readText())
        val lastReset = o.optString("lastResetDate", today)
        
        // Check if we need to reset the daily counter
        if (lastReset != today) {
            // New day - reset counter
            val updatedO = JSONObject().apply {
                put("count", 0)
                put("limit", DAILY_LIMIT)
                put("lastResetDate", today)
                put("firstUse", o.optString("firstUse", today))
            }
            f.writeText(updatedO.toString(2))
            return State(0, DAILY_LIMIT, today, o.optString("firstUse", today))
        }
        
        return State(
            o.optInt("count", 0),
            o.optInt("limit", DAILY_LIMIT),
            lastReset,
            o.optString("firstUse", today)
        )
    }

    fun increment(context: Context) {
        val f = File(context.filesDir, FILE)
        val st = state(context) // This already handles daily reset
        val newCount = (st.count + 1).coerceAtMost(Int.MAX_VALUE)
        val o = JSONObject().apply {
            put("count", newCount)
            put("limit", st.limit)
            put("lastResetDate", st.lastResetDate)
            put("firstUse", st.firstUseDate)
        }
        f.writeText(o.toString(2))
    }

    fun remaining(context: Context): Int {
        val s = state(context)
        return (s.limit - s.count).coerceAtLeast(0)
    }

    fun exhausted(context: Context): Boolean = remaining(context) <= 0
}
