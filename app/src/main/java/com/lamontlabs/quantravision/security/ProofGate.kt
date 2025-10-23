package com.lamontlabs.quantravision.security

import android.content.Context
import com.lamontlabs.quantravision.export.EvidenceExporter
import java.io.File

/**
 * ProofGate
 * - Enforces legal and deterministic checks before allowing export.
 * - Blocks if license invalid, disclaimers missing, or audit fails.
 */
object ProofGate {

    data class GateResult(val ok: Boolean, val message: String)

    fun assertClean(context: Context): GateResult {
        val lic = LicenseVerifier.verify(context)
        if (!lic.valid) return GateResult(false, "License invalid: ${lic.reason}")

        val disclaimer = File(context.filesDir, "DISCLAIMER.txt")
        if (!disclaimer.exists()) {
            return GateResult(false, "Missing disclaimer acknowledgement.")
        }

        val exporterOk = EvidenceExporter.lastAuditClean()
        if (!exporterOk) return GateResult(false, "Exporter audit failed")

        return GateResult(true, "ProofGate clear")
    }
}
