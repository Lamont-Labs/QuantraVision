package com.lamontlabs.quantravision.debug

import android.content.Context
import android.os.Build
import com.lamontlabs.quantravision.logging.Ledger
import com.lamontlabs.quantravision.security.IntegrityWatcher
import java.io.File
import java.security.MessageDigest

/**
 * ProofMode
 * Displays verifiable build identity and integrity metrics.
 * - Build fingerprint, SBOM hash, ledger lines, integrity state.
 * - No sensitive data, safe for public demos.
 */
object ProofMode {

    data class ProofReport(
        val device: String,
        val build: String,
        val sbomHash: String,
        val ledgerLines: Int,
        val integrityLocked: Boolean
    )

    fun generate(context: Context): ProofReport {
        val sbom = File(context.filesDir, "dist/sbom.json")
        val ledger = Ledger.file(context)
        val sbomHash = if (sbom.exists()) sha256(sbom) else "none"
        val ledgerCount = if (ledger.exists()) ledger.readLines().size else 0
        val locked = IntegrityWatcher.locked
        return ProofReport(
            device = Build.MODEL,
            build = Build.FINGERPRINT,
            sbomHash = sbomHash,
            ledgerLines = ledgerCount,
            integrityLocked = locked
        )
    }

    private fun sha256(f: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        f.inputStream().use { stream ->
            val buf = ByteArray(65536)
            while (true) {
                val r = stream.read(buf)
                if (r <= 0) break
                md.update(buf, 0, r)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
