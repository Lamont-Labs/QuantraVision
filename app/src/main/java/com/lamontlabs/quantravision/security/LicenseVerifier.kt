package com.lamontlabs.quantravision.security

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.*

/**
 * LicenseVerifier
 * - Enforces device-bound offline licensing
 * - Uses local license.json with expiration, device hash, and digital signature
 * - Locks out capture/export if invalid
 *
 * Structure:
 * {
 *   "licensee": "Jesse J. Lamont",
 *   "issued": "2025-10-21",
 *   "expires": "2028-10-21",
 *   "device_id": "<sha256>",
 *   "signature": "<hex Ed25519>"
 * }
 */
object LicenseVerifier {

    private const val FILE = "license.json"

    data class Result(
        val valid: Boolean,
        val reason: String,
        val expires: String
    )

    fun verify(context: Context): Result {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) {
            return Result(false, "License file missing", "")
        }
        val obj = JSONObject(f.readText())
        val expires = obj.optString("expires", "")
        val expTime = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(expires)?.time ?: 0L }.getOrDefault(0L)
        if (System.currentTimeMillis() > expTime) {
            return Result(false, "License expired", expires)
        }

        val devHash = deviceHash(context)
        val expected = obj.optString("device_id", "")
        if (devHash != expected) {
            return Result(false, "Device mismatch", expires)
        }

        val sig = obj.optString("signature", "")
        val payload = obj.toString().replace(sig, "")
        val ok = SignatureVerifier.verify(payload.toByteArray(), sig)
        return if (ok) Result(true, "Valid license", expires) else Result(false, "Invalid signature", expires)
    }

    private fun deviceHash(context: Context): String {
        val id = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(id.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
