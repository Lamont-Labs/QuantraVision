package com.lamontlabs.quantravision.quota

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

/**
 * HighlightQuota
 * Limits total pattern highlight events before Pro is required.
 * Persistent, device-local, deterministic JSON: highlight_quota.json
 */
object HighlightQuota {

    private const val FILE = "highlight_quota.json"
    private const val FREE_LIMIT_DEFAULT = 5

    data class State(val count: Int, val limit: Int, val firstUseDate: String)

    fun state(context: Context): State {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val o = JSONObject().apply {
                put("count", 0)
                put("limit", FREE_LIMIT_DEFAULT)
                put("firstUse", today)
            }
            f.writeText(o.toString(2))
            return State(0, FREE_LIMIT_DEFAULT, today)
        }
        val o = JSONObject(f.readText())
        return State(
            o.optInt("count", 0),
            o.optInt("limit", FREE_LIMIT_DEFAULT),
            o.optString("firstUse", "unknown")
        )
    }

    fun increment(context: Context) {
        val f = File(context.filesDir, FILE)
        val st = state(context)
        val newCount = (st.count + 1).coerceAtMost(Int.MAX_VALUE)
        val o = JSONObject().apply {
            put("count", newCount)
            put("limit", st.limit)
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
