package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.lamontlabs.quantravision.export.EvidenceExporter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * AnnotationManager
 * Local, deterministic annotation system for user notes.
 * - Notes attach to pattern detections by patternName + timestamp.
 * - Stored offline under /files/annotations.json for persistence.
 * - Included automatically in EvidenceExporter bundles.
 */
class AnnotationManager(private val context: Context) {

    data class Annotation(
        val id: String,
        val patternName: String,
        val timestamp: Long,
        val note: String
    )

    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    private val annotations = mutableStateListOf<Annotation>()
    private val file = File(context.filesDir, "annotations.json")

    init {
        load()
    }

    fun add(patternName: String, note: String) {
        val entry = Annotation(
            id = UUID.nameUUIDFromBytes((patternName + System.currentTimeMillis()).toByteArray()).toString(),
            patternName = patternName,
            timestamp = System.currentTimeMillis(),
            note = note.trim()
        )
        annotations.add(entry)
        save()
    }

    fun list(): List<Annotation> = annotations.toList()

    fun delete(id: String) {
        annotations.removeAll { it.id == id }
        save()
    }

    private fun load() {
        if (!file.exists()) return
        runCatching {
            val text = file.readText()
            val arr = org.json.JSONArray(text)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                annotations.add(
                    Annotation(
                        id = o.getString("id"),
                        patternName = o.getString("patternName"),
                        timestamp = o.getLong("timestamp"),
                        note = o.getString("note")
                    )
                )
            }
        }
    }

    private fun save() {
        val arr = org.json.JSONArray()
        annotations.forEach {
            arr.put(
                org.json.JSONObject().apply {
                    put("id", it.id)
                    put("patternName", it.patternName)
                    put("timestamp", sdf.format(Date(it.timestamp)))
                    put("note", it.note)
                }
            )
        }
        file.writeText(arr.toString(2))
    }

    fun includeInEvidenceBundle() {
        val distDir = File(context.filesDir, "dist").apply { mkdirs() }
        val dest = File(distDir, "annotations_snapshot.json")
        file.copyTo(dest, overwrite = true)
    }
}
