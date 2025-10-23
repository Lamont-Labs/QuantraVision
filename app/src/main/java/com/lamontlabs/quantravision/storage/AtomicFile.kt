package com.lamontlabs.quantravision.storage

import java.io.File
import java.io.FileOutputStream

/**
 * AtomicFile
 * Deterministic safe writer for critical files (ledger, templates, consent).
 * Writes to .tmp first, fsync, then rename.
 */
object AtomicFile {

    fun write(file: File, text: String) {
        val tmp = File(file.parentFile, file.name + ".tmp")
        FileOutputStream(tmp).use { out ->
            out.write(text.toByteArray())
            out.fd.sync()
        }
        if (file.exists()) file.delete()
        tmp.renameTo(file)
    }
}
