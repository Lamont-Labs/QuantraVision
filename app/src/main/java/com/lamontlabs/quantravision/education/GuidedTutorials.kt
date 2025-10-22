package com.lamontlabs.quantravision.education

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * GuidedTutorials
 * Offline, deterministic walkthroughs for pattern recognition.
 * JSON-based scripts stored in /files/tutorials.json.
 * Each tutorial step includes chart reference and description.
 */
object GuidedTutorials {

    data class Step(
        val id: String,
        val pattern: String,
        val description: String,
        val imagePath: String
    )

    private fun load(context: Context): List<Step> {
        val file = File(context.filesDir, "tutorials.json")
        if (!file.exists()) return emptyList()
        val text = file.readText()
        val arr = JSONArray(text)
        val steps = mutableListOf<Step>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            steps.add(
                Step(
                    id = o.getString("id"),
                    pattern = o.getString("pattern"),
                    description = o.getString("description"),
                    imagePath = o.getString("imagePath")
                )
            )
        }
        return steps
    }

    fun getTutorial(context: Context, pattern: String): List<Step> =
        load(context).filter { it.pattern == pattern }

    fun ensureDefault(context: Context) {
        val f = File(context.filesDir, "tutorials.json")
        if (f.exists()) return
        val default = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "tut_head_shoulders")
                put("pattern", "Head & Shoulders")
                put("description", "Recognize the formation of a peak (head) flanked by two smaller peaks (shoulders).")
                put("imagePath", "pattern_templates/head_shoulders_ref.png")
            })
            put(JSONObject().apply {
                put("id", "tut_double_top")
                put("pattern", "Double Top")
                put("description", "Identify two consecutive peaks at roughly the same price level.")
                put("imagePath", "pattern_templates/double_top_ref.png")
            })
        }
        f.writeText(default.toString(2))
    }
}
