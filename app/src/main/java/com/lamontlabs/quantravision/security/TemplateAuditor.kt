package com.lamontlabs.quantravision.security

import android.content.Context
import java.io.File
import java.security.MessageDigest
import java.security.Signature
import java.security.PublicKey
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*

/**
 * TemplateAuditor
 * Verifies signature and hash of every pattern template file.
 * Uses Ed25519 public key from /files/trusted_keys/public.key.
 */
object TemplateAuditor {

    private const val TRUST_DIR = "trusted_keys"
    private const val KEY_FILE = "public.key"

    fun audit(context: Context): List<String> {
        val issues = mutableListOf<String>()
        val keyDir = File(context.filesDir, TRUST_DIR)
        val pubKeyFile = File(keyDir, KEY_FILE)
        if (!pubKeyFile.exists()) {
            issues.add("Missing trusted key")
            return issues
        }
        val keyBytes = Base64.getDecoder().decode(pubKeyFile.readText().trim())
        val kf = KeyFactory.getInstance("Ed25519")
        val pubKey: PublicKey = kf.generatePublic(X509EncodedKeySpec(keyBytes))

        val templatesDir = File(context.filesDir, "pattern_templates")
        if (!templatesDir.exists()) {
            issues.add("Missing pattern_templates directory")
            return issues
        }

        templatesDir.listFiles { f -> f.extension == "yaml" }?.forEach { f ->
            val sigFile = File(f.parentFile, f.nameWithoutExtension + ".sig")
            if (!sigFile.exists()) {
                issues.add("No signature for ${f.name}")
                return@forEach
            }
            val sigBytes = Base64.getDecoder().decode(sigFile.readText().trim())
            val contentHash = sha256(f)
            val sig = Signature.getInstance("Ed25519")
            sig.initVerify(pubKey)
            sig.update(contentHash.toByteArray())
            val valid = sig.verify(sigBytes)
            if (!valid) issues.add("Invalid signature for ${f.name}")
        }
        return issues
    }

    private fun sha256(f: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        f.inputStream().use { s ->
            val buf = ByteArray(65536)
            while (true) {
                val r = s.read(buf)
                if (r <= 0) break
                md.update(buf, 0, r)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
