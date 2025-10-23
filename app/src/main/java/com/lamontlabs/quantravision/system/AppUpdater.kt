package com.lamontlabs.quantravision.system

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * AppUpdater
 * Optional offline/hosted checker. If /files/version.json exists, compares to current.
 * If newer version detected, writes a flag for UI to notify user.
 *
 * version.json schema:
 * { "latest_code": 11, "latest_name": "1.2" }
 */
object AppUpdater {

    private const val FLAG = "update_available.flag"

    fun checkLocal(context: Context, currentCode: Int): Boolean {
        val ver = File(context.filesDir, "version.json")
        if (!ver.exists()) return false
        return runCatching {
            val o = JSONObject(ver.readText())
            val latest = o.optInt("latest_code", currentCode)
            val available = latest > currentCode
            File(context.filesDir, FLAG).apply { if (available) writeText("1") else delete() }
            available
        }.getOrElse { false }
    }

    fun isUpdateFlagged(context: Context): Boolean =
        File(context.filesDir, FLAG).exists()
}
