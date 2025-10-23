package com.lamontlabs.quantravision.export

import android.content.Context
import java.io.File
import com.lamontlabs.quantravision.security.BundleSigner
import com.lamontlabs.quantravision.security.ProofGate

/**
 * EvidenceExporter
 * Wraps ProofGate before packaging evidence bundle.
 */
object EvidenceExporter {

    private var lastAuditOk = false

    fun lastAuditClean() = lastAuditOk

    fun export(context: Context): Boolean {
        val gate = ProofGate.assertClean(context)
        if (!gate.ok) {
            File(context.filesDir, "export_fail.log").writeText(gate.message)
            lastAuditOk = false
            return false
        }
        val outDir = File(context.filesDir, "export_bundle").apply { mkdirs() }
        val manifest = File(outDir, "manifest.json")
        manifest.writeText("{\"timestamp\":${System.currentTimeMillis()},\"proof\":\"ok\"}")
        BundleSigner.sign(outDir)
        lastAuditOk = true
        return true
    }
}
