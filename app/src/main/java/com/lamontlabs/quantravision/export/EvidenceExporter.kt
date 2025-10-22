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
 * Output: dist/quantravision_<timestamp>.evidence.zip
 * Contents:
 *  - evidence.yaml   (sorted, stable YAML)
 *  - sbom.json       (if present)
 *  - hashes.sha256   (pattern template hashes if present)
 */
object EvidenceExporter {

    private val TS_FMT = SimpleDateFormat("yyyy-MM-dd'T'HHmmss'Z'", Locale.US)

    fun export(context: Context): File {
        val outDir = File(context.filesDir, "dist").apply { mkdirs() }
        val ts = TS_FMT.format(System.currentTimeMillis())
        val zipFile = File(outDir, "quantravision_${ts}.evidence.zip")

        val yaml = buildEvidenceYaml(context)

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            // evidence.yaml
            putEntry(zos, "evidence.yaml", yaml.toByteArray(Charset.forName("UTF-8")))

            // sbom.json (optional)
            val sbom = File(outDir, "sbom.json")
            if (sbom.exists()) {
                putEntry(zos, "sbom.json", sbom.readBytes())
            }

            // hashes.sha256 (optional from templates dir)
            val hashes = File(File(context.filesDir, "pattern_templates"), "hashes.sha256")
            if (hashes.exists()) {
                putEntry(zos, "hashes.sha256", hashes.readBytes())
            }
        }

        return zipFile
    }

    private fun buildEvidenceYaml(context: Context): String {
        val db = PatternDatabase.getInstance(context)
        val rows = db.patternDao().getAll().sortedWith(
            compareBy({ it.patternName.lowercase(Locale.US) }, { it.timestamp })
        )

        val sb = StringBuilder()
        sb.appendLine("evidence_bundle:")
        sb.appendLine("  project: \"QuantraVision Overlay\"")
        sb.appendLine("  generated: \"${TS_FMT.format(System.currentTimeMillis())}\"")
        sb.appendLine("  deterministic: true")
        sb.appendLine("  entry_count: ${rows.size}")
        sb.appendLine("entries:")

        rows.forEach { r ->
            sb.appendLine("  - pattern: \"${r.patternName}\"")
            sb.appendLine("    timeframe: \"${r.timeframe}\"")
            sb.appendLine("    confidence: ${"%.4f".format(r.confidence)}")
            sb.appendLine("    consensus: ${"%.4f".format(r.consensusScore)}")
            sb.appendLine("    scale: ${"%.3f".format(r.scale)}")
            sb.appendLine("    timestamp: ${r.timestamp}")
        }

        // Stable footer hash over the YAML body (deterministic provenance)
        val body = sb.toString()
        val sha = sha256(body.toByteArray(Charset.forName("UTF-8")))
        return buildString {
            append(body)
            appendLine("provenance:")
            appendLine("  sha256: \"$sha\"")
        }
    }

    private fun putEntry(zos: ZipOutputStream, name: String, data: ByteArray) {
        val entry = ZipEntry(name)
        entry.time = 0L // zero for determinism
        zos.putNextEntry(entry)
        zos.write(data)
        zos.closeEntry()
    }

    private fun sha256(data: ByteArray): String {
        val d = MessageDigest.getInstance("SHA-256").digest(data)
        return d.joinToString("") { "%02x".format(it) }
    }
}
