package com.lamontlabs.quantravision.logging

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Ledger
 * Immutable detection ledger.
 * Now includes legal disclaimer footer for audit and compliance.
 */
object Ledger {

    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    private const val fileName = "ledger.log"

    fun append(context: Context, m: PatternMatch, hash: String) {
        val line = buildString {
            append(sdf.format(Date(m.timestamp)))
            append(" | ${m.patternName}")
            append(" | conf=${"%.3f".format(m.confidence)}")
            append(" | tf=${m.timeframe}")
            append(" | sha256=$hash")
        }
        val file = File(context.filesDir, fileName)
        file.appendText(line + "\n")
    }

    fun finalizeLedger(context: Context) {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return
        val disclaimer = """
            
            # DISCLAIMER
            # QuantraVision Overlay © 2025 Lamont Labs
            # Visual analysis only. No trades, predictions, or advice.
        """.trimIndent()
        file.appendText(disclaimer + "\n")
    }

    fun file(context: Context): File = File(context.filesDir, fileName)
}
