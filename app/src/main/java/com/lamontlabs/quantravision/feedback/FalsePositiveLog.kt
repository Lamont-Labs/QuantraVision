package com.lamontlabs.quantravision.feedback

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/**
 * FalsePositiveLog
 * - Records and suppresses user-reported false positives.
 * - 100% offline; hashes input to avoid saving chart data.
 */
object FalsePositiveLog {

    private const val FILE = "false_positives.json"

    fun record(context: Context, imagePath: String, patternId: String) {
        val f = File(context.filesDir, FILE)
        val obj = if (f.exists()) JSONObject(f.readText()) else JSONObject()
        val h = hash(imagePath + patternId)
        obj.put(h, System.currentTimeMillis())
        f.writeText(obj.toString(2))
    }

    fun isSuppressed(context: Context, imagePath: String, patternId: String): Boolean {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) return false
        val obj = JSONObject(f.readText())
        val h = hash(imagePath + patternId)
        return obj.has(h)
    }

    private fun hash(s: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
