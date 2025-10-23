package com.lamontlabs.quantravision.licensing

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * ProFeatureGate
 * Single source of truth for Pro unlock.
 * Active if license_pro.json exists and verifies minimal schema.
 */
object ProFeatureGate {
    private const val FILE = "license_pro.json"

    fun isActive(context: Context): Boolean {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) return false
        return runCatching {
            val o = JSONObject(f.readText())
            o.optString("tier", "") == "PRO" && o.optLong("issued", 0L) > 0L
        }.getOrElse { false }
    }

    /** Offline institutional unlock writer (call only after verification elsewhere). */
    fun activateOffline(context: Context, issued: Long = System.currentTimeMillis()) {
        val f = File(context.filesDir, FILE)
        val o = JSONObject().apply {
            put("tier", "PRO")
            put("issued", issued)
        }
        f.writeText(o.toString(2))
    }
}
