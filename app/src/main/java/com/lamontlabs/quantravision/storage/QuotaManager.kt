package com.lamontlabs.quantravision.storage

import android.content.Context
import java.io.File

/**
 * QuotaManager
 * Prevents uncontrolled file growth in /files.
 * Deterministic: LRU deletion by modified time, respecting size caps.
 */
object QuotaManager {

    private const val MAX_TOTAL_MB = 200
    private const val MAX_FILES_PER_DIR = 500

    fun enforce(context: Context) {
        val root = context.filesDir
        val dirs = root.listFiles()?.filter { it.isDirectory } ?: return
        dirs.forEach { dir ->
            enforceDir(dir)
        }
        enforceTotal(root)
    }

    private fun enforceDir(dir: File) {
        val files = dir.listFiles()?.sortedBy { it.lastModified() } ?: return
        if (files.size <= MAX_FILES_PER_DIR) return
        val excess = files.size - MAX_FILES_PER_DIR
        files.take(excess).forEach { it.delete() }
    }

    private fun enforceTotal(root: File) {
        val totalBytes = root.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        val totalMb = totalBytes / (1024 * 1024)
        if (totalMb <= MAX_TOTAL_MB) return
        val files = root.walkTopDown().filter { it.isFile }.sortedBy { it.lastModified() }.toList()
        var bytesFreed = 0L
        for (f in files) {
            if (totalMb - (bytesFreed / (1024 * 1024)) <= MAX_TOTAL_MB) break
            bytesFreed += f.length()
            f.delete()
        }
    }
}
