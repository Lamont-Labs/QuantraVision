package com.lamontlabs.quantravision.util

import android.content.Context
import android.util.Log
import java.io.File
import java.security.MessageDigest

/**
 * SelfTestValidator
 * - Runs on app startup to verify all critical assets and templates are unaltered.
 * - Checksums model files, pattern templates, and rules.
 * - Fail-closed: throws SecurityException if any mismatch is detected.
 */
object SelfTestValidator {

    private data class Target(val path: String, val expectedSha: String)
    private val criticalFiles = listOf(
        Target("app/src/main/assets/pattern_detector.tflite", "EXPECTED_HASH_TFLITE"),
        Target("app/src/main/assets/patterns.json", "EXPECTED_HASH_JSON")
    )

    fun run(context: Context) {
        val root = File(context.applicationInfo.dataDir)
        var allGood = true
        criticalFiles.forEach { target ->
            val f = File(root, target.path)
            if (!f.exists()) {
                Log.e("SelfTest", "Missing critical file ${target.path}")
                allGood = false
            } else {
                val hash = sha256(f)
                if (!hash.equals(target.expectedSha, true)) {
                    Log.e("SelfTest", "Checksum mismatch for ${target.path}")
                    allGood = false
                }
            }
        }
        if (!allGood) {
            throw SecurityException("Self-test failed: integrity violation detected.")
        } else {
            Log.i("SelfTest", "All core assets verified OK.")
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buf = ByteArray(4096)
            var bytes: Int
            while (fis.read(buf).also { bytes = it } != -1) {
                digest.update(buf, 0, bytes)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
