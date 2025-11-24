package com.lamontlabs.quantravision.quota

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * HighlightQuota
 * Enforces daily pattern highlight quota for Free tier.
 * Option 1 Pricing: 3 highlights per day
 * Resets daily at 00:00 UTC
 * Persistent JSON: highlight_quota.json
 */
object HighlightQuota {

    private const val FILE = "highlight_quota.json"
    private const val DAILY_LIMIT = 3 // Free tier gets 3 highlights per day

    data class State(
        val count: Int,
        val limit: Int,
        val lastResetDate: String,
        val firstUseDate: String,
        val lastResetMs: Long
    )

    fun state(context: Context): State {
        val f = File(context.filesDir, FILE)
        val todayUTC = LocalDate.now(ZoneOffset.UTC).toString()
        val nowMs = System.currentTimeMillis()
        
        if (!f.exists()) {
            val o = JSONObject().apply {
                put("count", 0)
                put("limit", DAILY_LIMIT)
                put("lastResetDate", todayUTC)
                put("lastResetMs", nowMs)
                put("firstUse", todayUTC)
            }
            f.writeText(o.toString(2))
            return State(0, DAILY_LIMIT, todayUTC, todayUTC, nowMs)
        }
        
        val o = try {
            JSONObject(f.readText())
        } catch (e: Exception) {
            android.util.Log.e("HighlightQuota", "Corrupted quota file detected, recreating", e)
            f.delete()
            val newO = JSONObject().apply {
                put("count", 0)
                put("limit", DAILY_LIMIT)
                put("lastResetDate", todayUTC)
                put("lastResetMs", nowMs)
                put("firstUse", todayUTC)
            }
            f.writeText(newO.toString(2))
            return State(0, DAILY_LIMIT, todayUTC, todayUTC, nowMs)
        }
        
        val lastResetDateUTC = o.optString("lastResetDate", todayUTC)
        val lastResetMs = o.optLong("lastResetMs", nowMs)
        
        if (lastResetDateUTC != todayUTC) {
            val updatedO = JSONObject().apply {
                put("count", 0)
                put("limit", DAILY_LIMIT)
                put("lastResetDate", todayUTC)
                put("lastResetMs", nowMs)
                put("firstUse", o.optString("firstUse", todayUTC))
            }
            f.writeText(updatedO.toString(2))
            return State(0, DAILY_LIMIT, todayUTC, o.optString("firstUse", todayUTC), nowMs)
        }
        
        return State(
            o.optInt("count", 0),
            o.optInt("limit", DAILY_LIMIT),
            lastResetDateUTC,
            o.optString("firstUse", todayUTC),
            lastResetMs
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
            put("lastResetMs", st.lastResetMs)
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
