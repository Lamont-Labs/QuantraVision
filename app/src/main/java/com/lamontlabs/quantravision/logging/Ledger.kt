package com.lamontlabs.quantravision.logging

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Immutable ledger of every detection for reproducibility proof.
 * Appends line-by-line with timestamp, pattern, confidence, tf, scale, and sha256.
 * Included in all exports for audit verification.
 */
object Ledger {

    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    private const val fileName = "ledger.log"

    fun append(context: Context, m: PatternMatch, hash: String) {
        val line = buildString {
            append(sdf.format(Date(m.timestamp)))
            append(" | ")
            append(m.patternName)
            append(" | tf=")
            append(m.timeframe)
            append(" | conf=")
            append("%.3f".format(m.confidence))
            append(" | scale=")
            append("%.2f".format(m.scale))
            append(" | sha256=")
            append(hash)
        }
        File(context.filesDir, fileName).appendText(line + "\n")
    }

    fun file(context: Context): File = File(context.filesDir, fileName)
}
