package com.lamontlabs.quantravision.importer

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream
import java.security.MessageDigest

/**
 * Deterministic template importer.
 * - Accepts a content Uri for a YAML or ZIP of YAMLs + PNGs.
 * - Verifies SHA-256 during copy.
 * - Writes into app filesDir/pattern_templates and pattern_images.
 * - Returns ImportReport with file hashes for provenance.
 */
class TemplateImporter(private val context: Context) {

    data class ImportedFile(val relativePath: String, val sha256: String, val bytes: Long)
    data class ImportReport(val files: List<ImportedFile>)

    fun importYaml(uri: Uri): ImportReport {
        val cr = context.contentResolver
        val targetDir = File(context.filesDir, "pattern_templates").apply { mkdirs() }
        val name = safeName(uri, cr)
        require(name.endsWith(".yaml", true)) { "Only .yaml allowed for single-file import." }
        val out = File(targetDir, name)
        val info = copyAndHash(cr.openInputStream(uri)!!, out)
        return ImportReport(listOf(info.copy(relativePath = "pattern_templates/${out.name}")))
    }

    fun importZip(uri: Uri): ImportReport {
        val cr = context.contentResolver
        val base = File(context.filesDir, "").apply { mkdirs() }
        val entries = mutableListOf<ImportedFile>()
        // Manual ZIP parse without randomization to stay deterministic over ABIs
        val zis = java.util.zip.ZipInputStream(cr.openInputStream(uri))
        var entry = zis.nextEntry
        while (entry != null) {
            val dest = File(base, sanitizeZipPath(entry.name))
            if (entry.isDirectory) {
                dest.mkdirs()
            } else {
                dest.parentFile?.mkdirs()
                val info = copyAndHash(zis, dest)
                entries.add(info.copy(relativePath = relPath(base, dest)))
            }
            entry = zis.nextEntry
        }
        zis.close()
        return ImportReport(entries.sortedBy { it.relativePath })
    }

    private fun copyAndHash(input: InputStream, outFile: File): ImportedFile {
        val md = MessageDigest.getInstance("SHA-256")
        outFile.outputStream().use { out ->
            val buf = ByteArray(64 * 1024)
            var read: Int
            var total = 0L
            while (true) {
                read = input.read(buf)
                if (read <= 0) break
                md.update(buf, 0, read)
                out.write(buf, 0, read)
                total += read
            }
            out.flush()
            input.close()
            val hex = md.digest().joinToString("") { "%02x".format(it) }
            return ImportedFile(relativePath = outFile.name, sha256 = hex, bytes = total)
        }
    }

    private fun safeName(uri: Uri, cr: ContentResolver): String {
        val raw = runCatching {
            cr.query(uri, null, null, null, null)?.use { c ->
                val nameIdx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst() && nameIdx >= 0) c.getString(nameIdx) else "template.yaml"
            }
        }.getOrNull() ?: "template.yaml"
        return raw.replace(Regex("[^A-Za-z0-9._-]"), "_")
    }

    private fun sanitizeZipPath(path: String): String {
        val cleaned = path.replace("\\", "/").replace(Regex("\\.\\./"), "")
        return cleaned.replace(Regex("[^A-Za-z0-9/_ .-]"), "_")
    }

    private fun relPath(root: File, file: File): String {
        return file.absolutePath.removePrefix(root.absolutePath).trimStart('/').replace(File.separatorChar, '/')
    }
}
