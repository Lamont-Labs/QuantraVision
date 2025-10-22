package com.lamontlabs.quantravision.privacy

import android.content.Context
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * RetentionPolicy
 * Prunes local detections, logs, and screenshots older than N days.
 * Deterministic and offline.
 */
object RetentionPolicy {

    fun enforce(context: Context, days: Long = 30) {
        val cutoff = Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli()
        val dirs = listOf(
            File(context.filesDir, "dist"),
            File(context.filesDir, "screenshots"),
            File(context.filesDir, "pattern_logs")
        )
        dirs.forEach { dir ->
            if (!dir.exists()) return@forEach
            dir.listFiles()?.forEach { f ->
                if (f.lastModified() < cutoff) f.delete()
            }
        }
    }
}
