package com.lamontlabs.quantravision.export

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * EvidenceExporter
 * Deterministic export of detection evidence and assets.
 * Adds legal disclaimer footer for liability compliance.
 */
object EvidenceExporter {

    private val TS_FMT = SimpleDateFormat("yyyy-MM-dd'T'HHmmss'Z'", Locale.US)

    fun export(context: Context): File {
        val outDir = File(context.filesDir, "dist").apply { mkdirs() }
        val ts = TS_FMT.format(System.currentTimeMillis())
        val zipFile = File(outDir, "quantravision_${ts}.evidence.zip")

        val yaml = buildEvidenceYaml(context) + disclaimerFooter()

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            putEntry(zos, "evidence.yaml", yaml.toByteArray(Charset.forName("UTF-8")))
        }

        return zipFile
    }

    private fun buildEvidenceYaml(context: Context): String {
        val db = PatternDatabase.getInstance(context)
        val rows = db.patternDao().getAll().sortedBy { it.timestamp }
        val sb = StringBuilder()
        sb.appendLine("evidence_bundle:")
        sb.appendLine("  project: \"QuantraVision Overlay\"")
        sb.appendLine("  generated: \"${TS_FMT.format(System.currentTimeMillis())}\"")
        sb.appendLine("entries:")
        rows.forEach { r ->
            sb.appendLine("  - pattern: \"${r.patternName}\"")
            sb.appendLine("    confidence: ${"%.4f".format(r.confidence)}")
            sb.appendLine("    timeframe: \"${r.timeframe}\"")
            sb.appendLine("    timestamp: ${r.timestamp}")
        }
        val body = sb.toString()
        val sha = sha256(body.toByteArray(Charset.forName("UTF-8")))
        return buildString {
            append(body)
            appendLine("provenance:")
            appendLine("  sha256: \"$sha\"")
        }
    }

    private fun disclaimerFooter(): String = """
        
        # DISCLAIMER
        # QuantraVision is provided for educational visualization only.
        # It does not constitute financial advice or signal generation.
    """.trimIndent()

    private fun putEntry(zos: ZipOutputStream, name: String, data: ByteArray) {
        val entry = ZipEntry(name)
        entry.time = 0L
        zos.putNextEntry(entry)
        zos.write(data)
        zos.closeEntry()
    }

    private fun sha256(data: ByteArray): String {
        val d = MessageDigest.getInstance("SHA-256").digest(data)
        return d.joinToString("") { "%02x".format(it) }
    }
}
