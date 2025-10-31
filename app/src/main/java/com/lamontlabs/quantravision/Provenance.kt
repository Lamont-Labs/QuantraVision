package com.lamontlabs.quantravision

import android.content.Context
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class Provenance(private val context: Context) {
    private val logFile = File(context.filesDir, "provenance.log")
    private val maxLogSizeBytes = 10 * 1024 * 1024 // 10MB limit

    fun logHash(file: File, patternName: String, scale: Double, aspect: Double, confidence: Double) {
        try {
            // Check and rotate log if too large
            if (logFile.exists() && logFile.length() > maxLogSizeBytes) {
                rotateLog()
            }
            
            val hash = sha256(file.readBytes())
            val time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                .format(Date(System.currentTimeMillis()))
            val entry = "$time | file=${file.name} | pattern=$patternName | scale=${"%.4f".format(scale)} | aspect=${"%.3f".format(aspect)} | conf=${"%.4f".format(confidence)} | sha256=$hash\n"
            logFile.appendText(entry)
        } catch (e: java.io.IOException) {
            android.util.Log.e("Provenance", "Failed to write provenance log (disk full or I/O error)", e)
            // Don't crash the app - provenance logging is non-critical
        } catch (e: Exception) {
            android.util.Log.e("Provenance", "Unexpected error in provenance logging", e)
        }
    }
    
    private fun rotateLog() {
        try {
            val archiveFile = File(context.filesDir, "provenance.log.old")
            if (archiveFile.exists()) {
                archiveFile.delete() // Delete old archive to save space
            }
            logFile.renameTo(archiveFile)
            android.util.Log.i("Provenance", "Rotated provenance log (exceeded 10MB)")
        } catch (e: Exception) {
            android.util.Log.e("Provenance", "Failed to rotate log, will attempt to clear", e)
            // If rotation fails, clear the log to prevent disk full
            try {
                logFile.delete()
            } catch (deleteError: Exception) {
                android.util.Log.e("Provenance", "Failed to delete log file", deleteError)
            }
        }
    }

    private fun sha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(data)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
