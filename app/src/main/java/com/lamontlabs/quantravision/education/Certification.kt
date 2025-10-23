package com.lamontlabs.quantravision.education

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Certification
 * Awards deterministic local badges after completing tutorials.
 * No network, human-readable certificates under /files/certs/.
 */
object Certification {

    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    data class Cert(val name: String, val date: String, val signature: String)

    fun issue(context: Context, user: String, module: String): File {
        val dir = File(context.filesDir, "certs").apply { mkdirs() }
        val file = File(dir, "${module}_${fmt.format(Date())}.json")
        val cert = JSONObject()
        cert.put("user", user)
        cert.put("module", module)
        cert.put("issued", fmt.format(Date()))
        cert.put("signature", deterministicSignature(user, module))
        file.writeText(cert.toString(2))
        return file
    }

    private fun deterministicSignature(user: String, module: String): String {
        val base = "$user|$module|QuantraVision"
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(base.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
