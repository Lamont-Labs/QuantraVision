package com.lamontlabs.quantravision

import android.content.Context
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class Provenance(private val context: Context) {
    private val logFile = File(context.filesDir, "provenance.log")

    fun logHash(file: File, patternName: String, scale: Double, aspect: Double, confidence: Double) {
        val hash = sha256(file.readBytes())
        val time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .format(Date(System.currentTimeMillis()))
        val entry = "$time | file=${file.name} | pattern=$patternName | scale=${"%.4f".format(scale)} | aspect=${"%.3f".format(aspect)} | conf=${"%.4f".format(confidence)} | sha256=$hash\n"
        logFile.appendText(entry)
    }

    private fun sha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(data)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
