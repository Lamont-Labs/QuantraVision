package com.lamontlabs.quantravision.analysis

import android.content.Context
import android.graphics.Bitmap
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.storage.AtomicFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * SessionRecorder
 * Records deterministic session logs of detections and optional screenshots.
 * No network. Stored locally under /files/sessions/<timestamp>/.
 */
object SessionRecorder {

    private val fmt = SimpleDateFormat("yyyy-MM-dd'T'HHmmss'Z'", Locale.US)

    fun startSession(context: Context): File {
        val dir = File(context.filesDir, "sessions/${fmt.format(Date())}")
        dir.mkdirs()
        return dir
    }

    fun recordDetection(sessionDir: File, match: PatternMatch) {
        val logFile = File(sessionDir, "detections.log")
        val entry = "${fmt.format(Date(match.timestamp))} | ${match.patternName} | conf=${"%.3f".format(match.confidence)} | tf=${match.timeframe}\n"
        AtomicFile.write(logFile, (logFile.takeIf { it.exists() }?.readText() ?: "") + entry)
    }

    fun saveScreenshot(sessionDir: File, bmp: Bitmap) {
        val out = File(sessionDir, "shot_${System.currentTimeMillis()}.png")
        out.outputStream().use { s ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, s)
        }
    }
}
