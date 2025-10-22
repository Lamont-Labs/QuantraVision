package com.lamontlabs.quantravision.safety

import android.content.Context
import java.io.File
import java.security.MessageDigest

/**
 * Deterministic integrity monitor.
 * Hashes key asset directories at startup and periodically during runtime.
 * If a mismatch is found, sets global lock flag for fail-closed behavior.
 */
object IntegrityWatcher {

    @Volatile
    var locked: Boolean = false
        private set

    data class Result(val file: String, val ok: Boolean, val hash: String)

    fun verifyAssets(context: Context): List<Result> {
        val base = File(context.filesDir, "pattern_templates")
        if (!base.exists()) return emptyList()
        val results = mutableListOf<Result>()
        base.walkTopDown().filter { it.isFile }.forEach { f ->
            val current = sha256(f)
            val expected = loadStoredHash(f)
            val ok = expected == null || expected == current
            results.add(Result(f.name, ok, current))
            if (!ok) locked = true
        }
        return results
    }

    private fun sha256(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buf = ByteArray(65536)
            while (true) {
                val r = fis.read(buf)
                if (r <= 0) break
                md.update(buf, 0, r)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    private fun loadStoredHash(file: File): String? {
        val tag = File(file.parentFile, "hashes.sha256")
        if (!tag.exists()) return null
        return tag.readLines()
            .firstOrNull { it.contains(file.name) }
            ?.split(" ")?.firstOrNull()
    }
}
