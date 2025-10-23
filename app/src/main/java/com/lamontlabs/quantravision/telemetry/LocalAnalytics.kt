package com.lamontlabs.quantravision.telemetry

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * LocalAnalytics
 * Counts detections by type and timestamp.
 * Entirely offline, human-readable, opt-in only.
 */
object LocalAnalytics {

    private const val FILE = "analytics.json"
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun record(context: Context, pattern: String) {
        val f = File(context.filesDir, FILE)
        val root = if (f.exists()) JSONObject(f.readText()) else JSONObject()
        val today = fmt.format(Date())
        val dayObj = root.optJSONObject(today) ?: JSONObject()
        val count = dayObj.optInt(pattern, 0) + 1
        dayObj.put(pattern, count)
        root.put(today, dayObj)
        f.writeText(root.toString(2))
    }

    fun getSummary(context: Context): JSONObject? {
        val f = File(context.filesDir, FILE)
        return if (f.exists()) JSONObject(f.readText()) else null
    }
}
