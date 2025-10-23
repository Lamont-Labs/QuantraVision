package com.lamontlabs.quantravision.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Watchlist
 * Offline list of user-selected instruments for study.
 * Stored deterministically under watchlist.json.
 */
object Watchlist {

    private const val fileName = "watchlist.json"

    fun add(context: Context, symbol: String) {
        val current = get(context).toMutableSet()
        current.add(symbol.uppercase())
        save(context, current.toList())
    }

    fun remove(context: Context, symbol: String) {
        val current = get(context).toMutableSet()
        current.remove(symbol.uppercase())
        save(context, current.toList())
    }

    fun get(context: Context): List<String> {
        val f = File(context.filesDir, fileName)
        if (!f.exists()) return emptyList()
        val arr = JSONArray(f.readText())
        return List(arr.length()) { arr.getString(it) }
    }

    private fun save(context: Context, list: List<String>) {
        val arr = JSONArray()
        list.sorted().forEach { arr.put(it) }
        File(context.filesDir, fileName).writeText(arr.toString(2))
    }
}
