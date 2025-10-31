package com.lamontlabs.quantravision.licensing

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * StandardFeatureGate
 * Single source of truth for Standard tier unlock.
 * Active if license_standard.json exists and verifies minimal schema.
 */
object StandardFeatureGate {
    private const val FILE = "license_standard.json"

    fun isActive(context: Context): Boolean {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) return false
        return runCatching {
            val o = JSONObject(f.readText())
            o.optString("tier", "") == "STANDARD" && o.optLong("issued", 0L) > 0L
        }.getOrElse { false }
    }

    /** Offline institutional unlock writer (call only after verification elsewhere). */
    fun activateOffline(context: Context, issued: Long = System.currentTimeMillis()) {
        val f = File(context.filesDir, FILE)
        val o = JSONObject().apply {
            put("tier", "STANDARD")
            put("issued", issued)
        }
        f.writeText(o.toString(2))
    }
}
