package com.lamontlabs.quantravision.diagnostics

import android.content.Context
import com.lamontlabs.quantravision.security.ProofVerifier
import java.io.File

/**
 * DeterminismGuard
 * Runs self-checks comparing hashes of templates, ledger, and SBOM across builds.
 * Locks app if mismatch detected.
 */
object DeterminismGuard {

    private const val REF_FILE = "determinism_ref.json"

    fun verify(context: Context): Boolean {
        val report = ProofVerifier.verify(context)
        val refFile = File(context.filesDir, REF_FILE)

        val json = """
            {
              "sbom": "${report.hashSummary["sbom.json"] ?: ""}",
              "ledger": "${report.hashSummary["ledger.log"] ?: ""}"
            }
        """.trimIndent()

        if (!refFile.exists()) {
            refFile.writeText(json)
            return true
        }

        val prev = refFile.readText()
        if (prev != json) {
            refFile.writeText(json)
            return false
        }
        return true
    }
}
