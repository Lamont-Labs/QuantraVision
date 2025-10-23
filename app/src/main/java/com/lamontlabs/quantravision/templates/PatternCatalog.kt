package com.lamontlabs.quantravision.templates

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

/**
 * PatternCatalog
 * - Enumerates YAML pattern templates in /files/pattern_templates
 * - Persists enabled/disabled state deterministically in /files/pattern_catalog.json
 * - Provides search and bulk operations
 */
object PatternCatalog {

    data class Entry(
        val id: String,              // filename without extension
        val name: String,            // name: from YAML (fallback = id)
        val file: File,              // full path to YAML
        val enabled: Boolean         // current toggle
    )

    private const val CATALOG_FILE = "pattern_catalog.json"
    private const val DIR_NAME = "pattern_templates"

    fun list(context: Context, query: String? = null): List<Entry> {
        val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
        val states = loadState(context)
        val files = dir.listFiles { f -> f.isFile && f.extension.lowercase(Locale.US) in setOf("yaml", "yml") } ?: emptyArray()
        val entries = files.map { f ->
            val id = f.nameWithoutExtension
            val name = extractNameFromYaml(f) ?: id
            Entry(id, name, f, states.optBoolean(id, true))
        }.sortedBy { it.name.lowercase(Locale.US) }
        val q = query?.trim().orEmpty()
        return if (q.isEmpty()) entries else entries.filter { it.name.contains(q, true) || it.id.contains(q, true) }
    }

    fun setEnabled(context: Context, id: String, enabled: Boolean) {
        val states = loadState(context)
        states.put(id, enabled)
        saveState(context, states)
    }

    fun enableAll(context: Context) {
        val states = JSONObject()
        list(context).forEach { states.put(it.id, true) }
        saveState(context, states)
    }

    fun disableAll(context: Context) {
        val states = JSONObject()
        list(context).forEach { states.put(it.id, false) }
        saveState(context, states)
    }

    fun enabledIds(context: Context): Set<String> {
        return list(context).filter { it.enabled }.map { it.id }.toSet()
    }

    private fun loadState(context: Context): JSONObject {
        val f = File(context.filesDir, CATALOG_FILE)
        return if (f.exists()) JSONObject(f.readText()) else JSONObject()
    }

    private fun saveState(context: Context, obj: JSONObject) {
        File(context.filesDir, CATALOG_FILE).writeText(obj.toString(2))
    }

    private fun extractNameFromYaml(f: File): String? {
        // Lightweight parse: read first ~40 lines searching for "name:"
        var count = 0
        f.forEachLine { line ->
            if (count++ > 40) return@forEachLine
            val m = Regex("""^\s*name\s*:\s*["']?(.+?)["']?\s*$""").find(line)
            if (m != null) return m.groupValues[1].trim()
        }
        return null
    }
}
