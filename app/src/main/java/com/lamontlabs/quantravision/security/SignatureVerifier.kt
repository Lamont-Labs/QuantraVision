package com.lamontlabs.quantravision.security

import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import android.util.Base64

/**
 * SignatureVerifier
 * Static Ed25519 verifier used by LicenseVerifier and BundleSigner.
 */
object SignatureVerifier {

    // Public key compiled into app; private key never stored here.
    private const val PUBKEY_B64 =
        "MCowBQYDK2VwAyEAbGhr1MjgPMDwcPiKz6m3j4HWg2HzW8I1Eh0BxqPbq4E="

    fun verify(data: ByteArray, hexSignature: String): Boolean {
        return try {
            val pubKeyBytes = Base64.decode(PUBKEY_B64, Base64.DEFAULT)
            val pubKey = KeyFactory.getInstance("Ed25519").generatePublic(X509EncodedKeySpec(pubKeyBytes))
            val sig = Signature.getInstance("Ed25519")
            sig.initVerify(pubKey)
            sig.update(data)
            sig.verify(hexSignature.chunked(2).map { it.toInt(16).toByte() }.toByteArray())
        } catch (_: Exception) {
            false
        }
    }
}
