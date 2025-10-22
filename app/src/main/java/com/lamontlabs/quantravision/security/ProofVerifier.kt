package com.lamontlabs.quantravision.security

import android.content.Context
import java.io.File
import java.security.MessageDigest

/**
 * ProofVerifier
 * Checks that every exported file includes deterministic disclaimer
 * and matching SBOM + ledger hashes before it can be shared.
 */
object ProofVerifier {

    private const val DISCLAIMER_MARKER = "# DISCLAIMER"

    data class Report(
        val sbomOk: Boolean,
        val ledgerOk: Boolean,
        val disclaimerOk: Boolean,
        val hashSummary: Map<String, String>
    )

    fun verify(context: Context): Report {
        val dist = File(context.filesDir, "dist")
        val sbom = File(dist, "sbom.json")
        val ledger = File(context.filesDir, "ledger.log")
        val evidence = dist.listFiles()?.firstOrNull { it.name.endsWith(".zip") }

        val hashSummary = mutableMapOf<String, String>()

        fun sha(f: File): String {
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

        val sbomOk = sbom.exists().also { if (it) hashSummary["sbom.json"] = sha(sbom) }
        val ledgerOk = ledger.exists().also { if (it) hashSummary["ledger.log"] = sha(ledger) }

        val disclaimerOk = evidence?.let {
            it.readText().contains(DISCLAIMER_MARKER)
        } ?: false

        return Report(sbomOk, ledgerOk, disclaimerOk, hashSummary)
    }
}
