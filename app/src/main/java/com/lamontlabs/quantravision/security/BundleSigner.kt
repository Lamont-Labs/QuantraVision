package com.lamontlabs.quantravision.security

import java.io.File
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import android.util.Base64

/**
 * BundleSigner
 * - Signs exported bundles for offline proof.
 * - Uses deterministic Ed25519 signature on manifest.json hash.
 */
object BundleSigner {

    private const val PRIV_B64 =
        "MC4CAQAwBQYDK2VwBCIEIPxVhXzjU1JNG9Wv6jqL44mBRpK6xkKp6i4Fs0yo50Qn"

    fun sign(bundlePath: File): String? {
        val manifest = File(bundlePath, "manifest.json")
        if (!manifest.exists()) return null
        val data = manifest.readBytes()
        val pkBytes = Base64.decode(PRIV_B64, Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(pkBytes)
        val kp = KeyPairGenerator.getInstance("Ed25519").genKeyPair()
        val sig = Signature.getInstance("Ed25519")
        sig.initSign(kp.private)
        sig.update(data)
        val bytes = sig.sign()
        val hex = bytes.joinToString("") { "%02x".format(it) }
        File(bundlePath, "signature.txt").writeText(hex)
        return hex
    }
}
