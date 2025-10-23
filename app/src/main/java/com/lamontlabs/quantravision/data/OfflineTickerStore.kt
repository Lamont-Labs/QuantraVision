package com.lamontlabs.quantravision.data

import android.content.Context
import java.io.File

/**
 * OfflineTickerStore
 * Handles import of CSV price data for educational visualization.
 * CSV columns: timestamp,open,high,low,close,volume
 * Strictly offline; no API fetching.
 */
object OfflineTickerStore {

    private val dirName = "offline_data"

    fun importCsv(context: Context, csvText: String, symbol: String) {
        val dir = File(context.filesDir, dirName).apply { mkdirs() }
        val out = File(dir, "${symbol.uppercase()}.csv")
        out.writeText(csvText.trim())
    }

    fun listSymbols(context: Context): List<String> {
        val dir = File(context.filesDir, dirName)
        if (!dir.exists()) return emptyList()
        return dir.listFiles()?.map { it.nameWithoutExtension } ?: emptyList()
    }

    fun getData(context: Context, symbol: String): List<String> {
        val f = File(context.filesDir, "$dirName/${symbol.uppercase()}.csv")
        if (!f.exists()) return emptyList()
        return f.readLines()
    }
}
