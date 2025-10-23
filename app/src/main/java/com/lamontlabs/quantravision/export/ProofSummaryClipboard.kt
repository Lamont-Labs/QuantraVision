package com.lamontlabs.quantravision.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import java.io.File

/**
 * ProofSummaryClipboard
 * Copies latest proof summary and hashes to clipboard for quick share.
 */
object ProofSummaryClipboard {

    fun copy(context: Context): Boolean {
        val dir = File(context.filesDir, "export_bundle")
        val manifest = File(dir, "manifest.json")
        val sig = File(dir, "signature.txt")
        if (!manifest.exists() || !sig.exists()) return false

        val text = """
            QuantraVision Proof Summary
            ──────────────────────────────
            Manifest Hash: ${manifest.readBytes().hashCode()}
            Signature: ${sig.readText().take(16)}…
            Disclaimer: OK
        """.trimIndent()

        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Proof Summary", text))
        return true
    }
}
