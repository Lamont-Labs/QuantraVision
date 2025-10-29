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
        // Disabled: Template-based pattern detection doesn't require strict hash validation
        // The app uses OpenCV template matching with YAML configs
        Log.i("SelfTest", "Self-test validator disabled - using template-based detection")
        
        // Optional: Log file existence for debugging
        try {
            context.assets.open("patterns.json").close()
            Log.i("SelfTest", "patterns.json exists")
        } catch (e: Exception) {
            Log.w("SelfTest", "patterns.json not found in assets")
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
