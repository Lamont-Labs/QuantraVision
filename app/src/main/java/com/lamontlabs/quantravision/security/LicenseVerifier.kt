package com.lamontlabs.quantravision.security

import android.content.Context
import java.io.File
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

/**
 * LicenseVerifier
 * Offline license validation using Ed25519 digital signatures.
 * - Reads license.lic (Base64) and public.key from assets or /files/
 * - Verifies signature of "QuantraVision-License" string.
 * - No network, no expiry calls. Fail-closed if signature invalid.
 */
object LicenseVerifier {

    private const val LICENSE_FILE = "license.lic"
    private const val PUBLIC_KEY_FILE = "public.key"
    private const val PAYLOAD = "QuantraVision-License"

    fun verify(context: Context): Boolean {
        val baseDir = context.filesDir
        val licenseFile = File(baseDir, LICENSE_FILE)
        val publicFile = File(baseDir, PUBLIC_KEY_FILE)
        if (!licenseFile.exists() || !publicFile.exists()) return false

        val licenseBytes = Base64.getDecoder().decode(licenseFile.readText().trim())
        val keyBytes = Base64.getDecoder().decode(publicFile.readText().trim())
        val kf = KeyFactory.getInstance("Ed25519")
        val pubKey: PublicKey = kf.generatePublic(X509EncodedKeySpec(keyBytes))
        val sig = Signature.getInstance("Ed25519")
        sig.initVerify(pubKey)
        sig.update(PAYLOAD.toByteArray())
        val ok = sig.verify(licenseBytes)
        if (!ok) {
            File(baseDir, "lock.flag").writeText("LICENSE INVALID")
        }
        return ok
    }
}
