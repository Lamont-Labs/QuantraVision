package com.lamontlabs.quantravision.system

import android.content.Context
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * BackupManager
 * Exports settings, templates, and logs to a single ZIP in /files/dist/.
 * Import restores JSON and YAML files (no code execution).
 */
object BackupManager {

    private val includePaths = listOf(
        "pattern_templates",
        "consent.json",
        "pattern_catalog.json",
        "app_config.json",
        "highlight_quota.json",
        "watchlist.json",
        "sessions",
        "ledger.log"
    )

    fun export(context: Context): File {
        val outDir = File(context.filesDir, "dist").apply { mkdirs() }
        val out = File(outDir, "backup_${System.currentTimeMillis()}.zip")
        ZipOutputStream(out.outputStream()).use { zip ->
            includePaths.forEach { path ->
                val f = File(context.filesDir, path)
                if (!f.exists()) return@forEach
                if (f.isFile) {
                    addFile(zip, f, f.name)
                } else {
                    f.walkTopDown().filter { it.isFile }.forEach { file ->
                        val rel = file.absolutePath.removePrefix(f.parentFile.absolutePath + File.separator)
                        addFile(zip, file, rel)
                    }
                }
            }
        }
        return out
    }

    private fun addFile(zip: ZipOutputStream, file: File, name: String) {
        zip.putNextEntry(ZipEntry(name))
        file.inputStream().use { it.copyTo(zip) }
        zip.closeEntry()
    }
}
