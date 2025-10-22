package com.lamontlabs.quantravision.privacy

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * ConsentManager
 * Handles opt-in flags for optional analytics or accessibility.
 * No network. All values stored in consent.json (deterministic JSON).
 */
object ConsentManager {

    private val fileName = "consent.json"

    data class Consent(val analytics: Boolean, val accessibility: Boolean)

    fun get(context: Context): Consent {
        val f = File(context.filesDir, fileName)
        if (!f.exists()) return Consent(false, true)
        val o = JSONObject(f.readText())
        return Consent(
            analytics = o.optBoolean("analytics", false),
            accessibility = o.optBoolean("accessibility", true)
        )
    }

    fun set(context: Context, analytics: Boolean, accessibility: Boolean) {
        val obj = JSONObject().apply {
            put("analytics", analytics)
            put("accessibility", accessibility)
        }
        File(context.filesDir, fileName).writeText(obj.toString(2))
    }
}
